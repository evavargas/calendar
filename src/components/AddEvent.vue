<template>
  <v-card>
    <v-container>
      <v-form @submit.prevent="addEvent">
        <v-row>
          <v-col cols="12" sm="8">
            <v-text-field
              type="text"
              label="Name"
              v-model="name"
              clearable
              counter="25"
            ></v-text-field>
            <v-text-field
              type="text"
              label="Details"
              v-model="details"
              clearable
              counter="25"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="4">
            <v-color-picker
              hide-canvas
              hide-inputs
              hide-mode-switch
              hide-sliders
              mode="hexa"
              show-swatches
              swatches-max-height="100"
              v-model="color"
            ></v-color-picker>
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="12" sm="4">
            <v-text-field
              type="date"
              label="Start date"
              v-model="start"
              :min="end"
              clearable
            ></v-text-field>
            <v-text-field
              type="date"
              label="End date"
              v-model="end"
              :min="start"
              clearable
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="4">
            <v-switch v-model="allDay" :label="`All day`"></v-switch>
          </v-col>
          <v-col cols="12" sm="4" v-if="!allDay">
            <v-text-field
              type="time"
              label="Start at"
              v-model="startTime"
              clearable
            ></v-text-field>
            <v-text-field
              type="time"
              label="End at"
              v-model="endTime"
              clearable
            ></v-text-field>
          </v-col>
          <v-col cols="6">
            <v-btn block type="submit" color="success" @click.stop="addEdialogClose"
          plain outlined class="mb-3">Save Event</v-btn>
          </v-col>
          <v-col cols="6">
            <v-btn block color="warning" @click.stop="addEdialogClose"
          plain outlined class="mb-3">Cancel</v-btn>
          </v-col>
        </v-row>
      </v-form>
    </v-container>
  </v-card>
</template>

<script>
import { mapActions, mapMutations } from 'vuex';
export default {
  name: "AddEvent",
  data() {
    return {
      name: "",
      details: "",
      start: "",
      end: "",
      color: "",
      startTime: "",
      endTime: "",
      timed: false,
      allDay: false,
    };
  },
  computed:{
    datetimestart: function (){
      return this.allDay? (this.start) : (this.start+' '+this.startTime)
    },
    datetimeend : function(){
      return this.allDay? (this.end) : (this.end+' '+this.endTime) 
    },
    timedEvent: function (){
      return this.allDay? this.timed = false : this.timed = true;
    }
  },

methods:{
  clearEvent(){
    this.name= ""
      this.details= ""
      this.start= ""
      this.end= ""
      this.color= ""
      this.startTime= ""
      this.endTime= ""
      this.timed=false
  },
   addEvent(){
    try {
      if (this.name && this.start && this.end){
        let event ={
          name : this.name,
          details: this.details,
          start: this.datetimestart,
          end: this.datetimeend,
          color: this.color,
          timed: this.timed,
        }
        this.saveEvent(event)
        this.getEvents()
        this.clearEvent()
      }else{
        console.log('Required fields')
      }
    } catch (error) {
    
    }
  },
  ...mapMutations(['addEdialogClose']),
  ...mapActions(['saveEvent', 'getEvents'])
}};
</script>

<style>
</style>