package com.example.taxi.ui.home.tarif

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.model.tarif.ModeRequest
import com.example.taxi.domain.model.tarif.ModeResponse
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ModeViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) : ViewModel() {
    private val _modeResponse = MutableLiveData<Resource<ModeResponse>>()
    private val _modeRequest = MutableLiveData<Resource<ModeResponse>>()
    private val compositeDisposable = CompositeDisposable()
    val modeResponse: LiveData<Resource<ModeResponse>>
        get() = _modeResponse

    val modeRequest: LiveData<Resource<ModeResponse>>
        get() = _modeRequest


    fun getModes() {
        _modeResponse.value = Resource(ResourceState.LOADING)
        compositeDisposable.add(
            getMainResponseUseCase.getModes()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        _modeResponse.postValue(Resource(ResourceState.SUCCESS, response))
                    },
                    { error ->
                        _modeResponse.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )
    }

    fun setModes(request: ModeRequest) {
        _modeRequest.value = Resource(ResourceState.LOADING)
        compositeDisposable.add(
            getMainResponseUseCase.setModes(request = request)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        _modeRequest.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                data = response
                            )
                        )
                    },
                    { error ->
                        _modeRequest.postValue(
                            Resource(
                                ResourceState.ERROR,
                                message = traceErrorException(error).getErrorMessage()
                            )
                        )
                    }
                )
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}