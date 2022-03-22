<template>
  <v-row class="fill-height">
    <v-col>
      <v-sheet height="64">
        <v-toolbar flat>
          <v-btn
            outlined
            class="mr-4"
            color="grey darken-2"
            @click="addEdialogOpen"
          >
            Agregar evento
          </v-btn>
          <v-btn outlined class="mr-4" color="grey darken-2" @click="setToday">
            Hoy
          </v-btn>
          <v-btn fab text small color="grey darken-2" @click="prev">
            <v-icon small> mdi-chevron-left </v-icon>
          </v-btn>
          <v-btn fab text small color="grey darken-2" @click="next">
            <v-icon small> mdi-chevron-right </v-icon>
          </v-btn>
          <v-toolbar-title v-if="$refs.calendar">
            {{ $refs.calendar.title }}
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-menu bottom right>
            <template v-slot:activator="{ on, attrs }">
              <v-btn outlined color="grey darken-2" v-bind="attrs" v-on="on">
                <span>{{ typeToLabel[type] }}</span>
                <v-icon right> mdi-menu-down </v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item @click="type = 'day'">
                <v-list-item-title>Día</v-list-item-title>
              </v-list-item>
              <v-list-item @click="type = 'week'">
                <v-list-item-title>Semana</v-list-item-title>
              </v-list-item>
              <v-list-item @click="type = 'month'">
                <v-list-item-title>Mes</v-list-item-title>
              </v-list-item>
              <v-list-item @click="type = '4day'">
                <v-list-item-title>4 días</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </v-toolbar>
      </v-sheet>
      <v-sheet height="600">
        <v-calendar
          ref="calendar"
          v-model="focus"
          color="primary"
          :events="events"
          :event-color="getEventColor"
          :type="type"
          @click:event="showEvent"
          @click:more="viewDay"
          @click:date="viewDay"
          @change="updateEvents"
        ></v-calendar>
        <v-dialog v-model="addEdialog" persistent>
          <AddEvent/>
        </v-dialog>
        <v-menu
          v-model="selectedOpen"
          :close-on-content-click="false"
          :activator="selectedElement"
          offset-x
        >
          <v-card color="grey lighten-4" min-width="350px" flat>
            <v-toolbar :color="selectedEvent.color" dark>
              <v-btn icon @click="deleteEvent(selectedEvent)">
                <v-icon>mdi-delete</v-icon>
              </v-btn>
              <v-toolbar-title v-html="selectedEvent.name"></v-toolbar-title>
              <v-spacer></v-spacer>
            </v-toolbar>
            <v-card-text v-if="currentlyEditing!==selectedEvent.id">
              <span v-html="selectedEvent.details"></span>
            </v-card-text>
            <v-card-text v-else >
              <v-form>
                <v-text-field type="text" v-model="selectedEvent.details" ></v-text-field>
              </v-form>
            </v-card-text>
            <v-card-actions>
              <v-btn text color="secondary" @click="selectedOpen = false">
                Cancelar
              </v-btn>
              <v-btn v-if="currentlyEditing !== selectedEvent.id"
              text color="warning" @click.prevent="editEvent(selectedEvent.id)">
                Editar
              </v-btn>
              <v-btn v-else
              text color="success" @click.prevent="updateEvent(selectedEvent)">
                Guardar
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-menu>
      </v-sheet>
    </v-col>
  </v-row>
</template>

<script>
 import AddEvent from './AddEvent.vue'
import { mapActions, mapMutations, mapState } from "vuex";
export default {
  name: "Calendar",
  components: {
    AddEvent
  },
  data: () => ({
    focus: "",
    type: "month",
    typeToLabel: {
      month: "Mes",
      week: "Semana",
      day: "Día",
      "4day": "4 Días",
    },
    selectedEvent: {},
    selectedElement: null,
    selectedOpen: false,
    currentlyEditing: null,
  }),
  mounted() {
    this.$refs.calendar.checkChange();
  },
  created() {
    this.getEvents();
  },
  methods: {
    viewDay({ date }) {
      this.focus = date;
      this.type = "day";
    },
    getEventColor(event) {
      return event.color;
    },
    setToday() {
      this.focus = "";
    },
    prev() {
      this.$refs.calendar.prev();
    },
    next() {
      this.$refs.calendar.next();
    },
    showEvent({ nativeEvent, event }) {
      const open = () => {
        this.selectedEvent = event;
        this.selectedElement = nativeEvent.target;
        requestAnimationFrame(() =>
          requestAnimationFrame(() => (this.selectedOpen = true))
        );
      };

      if (this.selectedOpen) {
        this.selectedOpen = false;
        requestAnimationFrame(() => requestAnimationFrame(() => open()));
      } else {
        open();
      }

      nativeEvent.stopPropagation();
    },
    updateEvents(){
    },
    async deleteEvent(ev){
      try {
         await this.removeEvent(ev)
         this.selectedOpen= false;
         this.getEvents()
      } catch (error) {
        console.log(error)
      }
    },
    editEvent(id){
      this.currentlyEditing = id
    },
    async updateEvent(ev){
      try {
        await this.modifyEvent(ev)
        this.selectedOpen= false
        this.getEvents()
        this.currentlyEditing= null
        this.selectedElement = null
      } catch (error) {
        console.log(error)
      }
    },
    ...mapActions(['getEvents','removeEvent', 'modifyEvent']),
    ...mapMutations(['addEdialogOpen'])

  },
  computed:{
    ...mapState(['events','addEdialog'])
  }
};
</script>
<style scoped>
</style>