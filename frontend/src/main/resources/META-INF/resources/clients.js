
const API_URL = `frontend/clients`

export default {
  created() {
    // fetch on init
    this.fetchData()
  },
  data() {
    return {
        clients: []
    }
  },
  methods: {
      async fetchData() {
        const url = `${API_URL}`
        this.clients = await (await fetch(url)).json()
      }
  },
  template: `
    <h1>Clients</h1>
    <div class="card mt-3" style="width: 18rem;" v-for="client in clients">
      <div class="card-body">
        <h5 class="card-title">{{client.fullName}}</h5>
        <h6 class="card-subtitle mb-2 text-body-secondary">{{client.email}}</h6>
        <p class="card-text"><span class="badge bg-success">Active</span></p>
        <router-link :to="{ name: 'client', params: { id : client.id }}">
             View
        </router-link>
      </div>
    </div>
  `
}