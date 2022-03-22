import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify'

import { initializeApp } from "firebase/app";
import firebase from 'firebase/compat/app';
import { getFirestore } from "firebase/firestore"
import 'firebase/compat/firestore';

// Your web app's Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyAVBgmDfmv2KGrqBO2fk_QDk3sJPagHvVU",
  authDomain: "calendar-vue-star.firebaseapp.com",
  projectId: "calendar-vue-star",
  storageBucket: "calendar-vue-star.appspot.com",
  messagingSenderId: "885566235646",
  appId: "1:885566235646:web:393c79686422d66ce496af"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

//export const db = firebase.firestore()
export const db = getFirestore();

Vue.config.productionTip = false

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')
