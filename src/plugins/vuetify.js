import Vue from 'vue';
import Vuetify from 'vuetify/lib/framework';

Vue.use(Vuetify);
// Translation provided by Vuetify (javascript)
import es from 'vuetify/lib/locale/es'

// Translation provided by Vuetify (typescript)
import en from 'vuetify/lib/locale/en'
export default new Vuetify({
    lang: {
        locales: { es, en },
        current: 'es',
      }
});
