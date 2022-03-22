import Vue from 'vue'
import Vuex from 'vuex'

import {db} from '../main'
import { doc, addDoc, collection, getDocs, deleteDoc , updateDoc} from "firebase/firestore";

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    addEdialog: false,
    events: []
    //YYYY-MM-DD or YYYY-MM-DD hh:mm
  },
  mutations: {
    addEdialogOpen(state){
      state.addEdialog = true
    },
    addEdialogClose(state){
      state.addEdialog = false;
    },
    chargeEvents(state, events){
      state.events = events;
      console.log(state.events)
    }
  },
  actions: {
    modifyEvent: async function({commit}, ev){
      try {
        await updateDoc(doc(db, "events", ev.id),{
          details: ev.details 
        })
      } catch (error) {
        console.log(error)
      }
    },
    saveEvent: async function ({commit}, event){
      try {
        await addDoc(collection(db, "events"),event);
      } catch (error) {
        console.log(error)
      }
    },
    getEvents : async function({commit}) {
      try {
        const snapshot = await getDocs(collection(db, "events"));
        let eventsDB = [];
        snapshot.forEach((doc) => {
          let eventData = doc.data();
          eventData.id = doc.id;
          eventsDB.push(eventData);     
        });
        commit('chargeEvents',eventsDB)  
      } catch (error) {
        console.log(error);
      }
    },
    removeEvent: async function({commit}, event){
      try {
        await deleteDoc(doc(db, "events", event.id));
      } catch (error) {
        console.log(error)
      }
    }
  },
  modules: {
  }
})
