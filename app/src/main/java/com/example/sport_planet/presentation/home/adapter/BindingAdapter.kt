package com.example.sport_planet.presentation.home.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sport_planet.model.BoardModel

@BindingAdapter("bind_setHomeItems")
fun setHomeItems(view: RecyclerView, items: List<BoardModel>?) {
    items?.run {
        (view.adapter as HomeRecyclerAdapter).setItems(items)
    }
}