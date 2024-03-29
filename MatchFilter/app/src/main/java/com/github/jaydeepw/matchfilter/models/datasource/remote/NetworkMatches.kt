package com.github.jaydeepw.matchfilter.models.datasource.remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.jaydeepw.matchfilter.models.datasource.Loadable
import com.github.jaydeepw.matchfilter.models.datasource.MatchesDataSource
import com.github.jaydeepw.matchfilter.models.datasource.remote.restapi.ApiInterface
import com.github.jaydeepw.matchfilter.models.entities.MatchResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject

class NetworkMatches @Inject constructor(
    var api: ApiInterface
) : MatchesDataSource, Loadable {

    private lateinit var subscription: Disposable
    lateinit var loading: MutableLiveData<Boolean>
    lateinit var errorHandler: MutableLiveData<String>

    val list: MutableLiveData<Response<MatchResponse>> = MutableLiveData()

    override fun getMatches(map: HashMap<String, String>?): LiveData<Response<MatchResponse>> {
        val observable = api.getMatches(map)
        subscription = observable.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { onLoadingInProgress() }
            .doOnError { onLoadingComplete() }
            .doOnComplete { onLoadingComplete() }
            .subscribe({ handleResponse(it) }, { handleError(it) })
        return list
    }

    override fun onLoadingInProgress() {
        loading.value = true
    }

    override fun onLoadingComplete() {
        loading.value = false
    }

    private fun handleError(it: Throwable) {
        errorHandler.value = it.localizedMessage ?: it.message
    }

    private fun handleResponse(it: Response<MatchResponse>) {
        list.value = it
    }
}