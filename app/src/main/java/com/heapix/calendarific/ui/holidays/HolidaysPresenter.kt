package com.heapix.calendarific.ui.holidays

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.heapix.calendarific.MyApp
import com.heapix.calendarific.net.repo.CountryRepo
import com.heapix.calendarific.net.repo.HolidayRepo
import com.heapix.calendarific.net.repo.YearRepo
import com.heapix.calendarific.net.responses.country.CountryResponse
import com.heapix.calendarific.net.responses.holiday.HolidayResponse
import com.heapix.calendarific.ui.base.BaseMvpPresenter
import io.reactivex.Observable
import org.kodein.di.instance

@InjectViewState
class HolidaysPresenter : BaseMvpPresenter<HolidaysView>() {

    private val holidayRepo: HolidayRepo by MyApp.kodein.instance()
    private val countryRepo: CountryRepo by MyApp.kodein.instance()
    private val yearRepo: YearRepo by MyApp.kodein.instance()

    private lateinit var countryResponseList: MutableList<CountryResponse>

    fun onCreate(
        holidayItemClickObservable: Observable<HolidayResponse>,
        countryItemClickObservable: Observable<CountryResponse>
    ) {
        getHolidaysAndUpdateUi()
        getCountriesAndUpdateUi()
        getYearsAndUpdateUi()
        setupOnHolidayItemClickListener(holidayItemClickObservable)
        setupOnCountryItemClickListener(countryItemClickObservable)
    }

    private fun getHolidaysAndUpdateUi() {
        addDisposable(
            holidayRepo.getAllHolidays(getIso(), getYear())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    {
                        viewState.updateHolidays(it)
                    }, {
                        Log.e("TAG", it.toString())
                    }
                )
        )
    }

    private fun getCountriesAndUpdateUi() {
        addDisposable(
            countryRepo.getAllCountries()
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    {
                        countryResponseList = it
                        viewState.showChosenCountryName(getCountryName())
                        //viewState.showChosenCountryName(getIso())
                        viewState.updateCountries(it)
                    }, {
                        Log.e("TAG", it.toString())
                    }
                )
        )
    }

    private fun getYearsAndUpdateUi() {
        viewState.showChosenYear(getYear())
        viewState.updateYears(yearRepo.getAllYears())
    }

    private fun setupOnHolidayItemClickListener(holidayResponseItemClickObservable: Observable<HolidayResponse>) {
        addDisposable(
            holidayResponseItemClickObservable
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    {
                        showMessage("Holiday card is pressed")
                    }, {
                        Log.e("TAG", it.toString())
                    }
                )
        )
    }

    private fun setupOnCountryItemClickListener(countryResponseItemClickObservable: Observable<CountryResponse>) {
        addDisposable(
            countryResponseItemClickObservable
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    {
                        countryRepo.saveIso(it.iso)
                        getHolidaysAndUpdateUi()
                        viewState.hideKeyboard()
                        viewState.hideCountryList()
                        viewState.showChosenCountryName(it.countryName)
                    }, {
                        Log.e("TAG", it.toString())
                    }
                )
        )
    }

    fun onTextChanged(text: String) {
        viewState.updateCountries(
            countryResponseList.filter {
                it.countryName?.contains(
                    text,
                    ignoreCase = true
                ) ?: true
            }.toMutableList()
        )
    }

    fun onBackButtonClicked() {
        getHolidaysAndUpdateUi()
        viewState.hideYearPicker()
    }

    fun onNumberPickerScrolled(year: Int) {
        countryRepo.saveYear(year)
        viewState.showChosenYear(getYear())
    }

    private fun getIso(): String = countryRepo.getIso()

    private fun getYear(): Int = yearRepo.getYear()

    private fun getCountryName(): String = countryRepo.getCountryName()

    fun onCountryClicked() = viewState.showCountryList()

    fun onYearClicked() = viewState.showYearPicker(getYear())

}