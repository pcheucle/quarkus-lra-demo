
const CLIENT_API_URL = `frontend/clients/`

export default {
	props: ['id'],
	created() {
		// fetch on init
		this.fetchData()
	},
	data() {
		return {
			client: {},
			accounts: [],
			deleted: false,
			fetched: false
		}
	},
	methods: {
		async fetchData() {
		
			const urlClient = `${CLIENT_API_URL}` + this.id
			this.client = await (await fetch(urlClient)).json()

			const urlClientAccounts = `${CLIENT_API_URL}` + this.id + '/accounts'
			this.accounts = await (await fetch(urlClientAccounts)).json()
		
			this.fetched = true

		},
		async deleteClient() {

			var urlClient = `${CLIENT_API_URL}` + this.id
			await fetch(urlClient, { method: 'DELETE' })
			this.deleted = true

		}
	},
	watch: {
    	
    	deleted: 'fetchData'
  	},
	template: `
	<div v-if=fetched>
	    <h1>{{client.fullName}}</h1>
	    <p>{{client.email}}</p>
	    <p>
	    	<span v-if=client.deleted class="badge bg-secondary">Deleted</span>
	        <span v-else class="badge bg-success">Active</span>
	    </p>
	    
	    <p v-if=!client.deleted>
	    	<button type="button" class="btn btn-danger btn-sm" @click="deleteClient">Delete Client</button>
	    </p>
	
	    <hr>
	    <h3>Accounts</h3>
	    <div class="card mt-3" style="width: 18rem;" v-for="account in accounts">
	      <div class="card-body">
	        <h5 class="card-title">{{account.number}}</h5>
	        <h6 class="card-subtitle mb-2 text-body-secondary">{{account.balance}} €</h6>
	        <p class="card-text">
	        	<span v-if=account.closed class="badge bg-secondary">Closed</span>
	        	<span v-else class="badge bg-success">Open</span>
	        </p>
	      </div>
	    </div>
    
    </div>
  `
}