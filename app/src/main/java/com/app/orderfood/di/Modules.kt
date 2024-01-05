package com.app.orderfood.di

import com.app.orderfood.viewmodel.CartViewModel
import com.app.orderfood.viewmodel.CustomerViewModel
import com.app.orderfood.viewmodel.OrderViewModel
import com.app.orderfood.viewmodel.SellerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    //viewmodellerin koin ile enjeksiyonu 
    viewModel{
        CustomerViewModel(get())
    }

    viewModel{
        SellerViewModel(get())
    }

    viewModel{
        CartViewModel(get())
    }

    viewModel{
        OrderViewModel(get())
    }

}