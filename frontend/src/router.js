
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import OrderManager from "./components/OrderManager"

import PaymentHistoryManager from "./components/PaymentHistoryManager"

import ReservationManager from "./components/ReservationManager"


import MyPage from "./components/MyPage"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/paymentHistories',
                name: 'PaymentHistoryManager',
                component: PaymentHistoryManager
            },

            {
                path: '/reservations',
                name: 'ReservationManager',
                component: ReservationManager
            },


            {
                path: '/myPages',
                name: 'MyPage',
                component: MyPage
            },


    ]
})
