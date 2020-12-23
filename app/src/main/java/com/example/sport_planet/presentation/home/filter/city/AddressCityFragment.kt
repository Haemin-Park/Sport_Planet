package com.example.sport_planet.presentation.home.filter.city

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sport_planet.R
import com.example.sport_planet.data.response.basic.RegionResponse
import com.example.sport_planet.databinding.FragmentAddressCityBinding
import com.example.sport_planet.presentation.base.BaseFragment
import com.example.sport_planet.presentation.home.adapter.FilterCityGridViewAdapter
import com.example.sport_planet.presentation.home.filter.FilterActivity
import com.example.sport_planet.remote.RemoteDataSourceImpl
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo

class AddressCityFragment private constructor() :
    BaseFragment<FragmentAddressCityBinding, AddressCityViewModel>(R.layout.fragment_address_city) {
    override val viewModel: AddressCityViewModel by lazy {
        ViewModelProvider(
            this,
            AddressCityViewModelFactory(RemoteDataSourceImpl())
        ).get(AddressCityViewModel::class.java)
    }

    private lateinit var gridAdapter: FilterCityGridViewAdapter

    override fun init() {
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridAdapter = FilterCityGridViewAdapter {
            viewModel.clickItems(it)
        }
        binding.gvCity.adapter = gridAdapter

        viewModel.items.observe(viewLifecycleOwner, Observer {
            gridAdapter.setItems(it)
        })

        viewModel.selectedItems.observe(viewLifecycleOwner, Observer {
            gridAdapter.setSelectedItems(it)
            (activity as? FilterActivity)?.getCity(it)
        })

        viewModel.isLoading
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { if (it) showLoading() else hideLoading() }
            .addTo(compositeDisposable)

        viewModel.showErrorToast
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { showToast("최대 3개까지 선택 가능합니다") }
            .addTo(compositeDisposable)

        viewModel.selectedItems.value = arguments?.getParcelableArrayList(FilterActivity.INTENT_CITY) ?: emptyList()
    }

    fun getCity(): List<RegionResponse.Data> {
        return viewModel.items.value ?: emptyList()
    }

    fun clearCity() {
        viewModel.selectedItems.value = emptyList()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAddressCity()
    }

    companion object {
        fun newInstance(city: List<RegionResponse.Data>): AddressCityFragment {
            val args = Bundle()
            args.putParcelableArrayList(FilterActivity.INTENT_CITY, ArrayList(city))

            val fragment = AddressCityFragment()
            fragment.arguments = args
            return fragment
        }
    }
}