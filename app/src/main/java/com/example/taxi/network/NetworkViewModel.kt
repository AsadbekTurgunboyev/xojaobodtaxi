package com.example.taxi.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import retrofit2.HttpException

const val IS_CONNECT = "isConnect"
const val NO_CONNECT = "noConnect"
class NetworkViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val _response = MutableLiveData<Resource<String>>()
    val response: LiveData<Resource<String>> get() = _response

    fun getOrderCurrent() {
        _response.postValue(Resource(ResourceState.LOADING))

        compositeDisposable.add(getMainResponseUseCase.getOrderCurrent()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                // Perform any setup tasks before the subscription starts
            }.doOnTerminate {}.subscribe({ response ->
                _response.postValue(Resource(ResourceState.SUCCESS, IS_CONNECT))
            }, { error ->

                val errorMessage = if (error is HttpException) {
                    try {
                        val errorBody = error.response()?.errorBody()?.string()
                        val mainResponse = Gson().fromJson(errorBody, MainResponse::class.java)
                        if (mainResponse.status == 400 && mainResponse.success.not()){
                            NO_CONNECT
                        }else{
                            mainResponse.message
                        }
                    } catch (e: Exception) {
                        "An error occurred"
                    }
                } else {
                    "An error occurred"
                }

                _response.postValue(
                    Resource(
                        ResourceState.ERROR,
                        message = errorMessage
                    )
                )

            })
        )
    }

}