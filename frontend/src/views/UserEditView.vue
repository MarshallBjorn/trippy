<template>
  <div class="space-y-6 max-w-3xl mx-auto">
    
    <div class="flex items-center gap-4 border-b border-gray-200 pb-4">
      <button @click="router.back()" class="text-gray-500 hover:text-gray-700">
        &larr; Wróć do listy
      </button>
      <h2 class="text-2xl font-bold text-gray-800">Edycja użytkownika</h2>
    </div>

    <div v-if="loading" class="text-center py-10 text-gray-500">Pobieranie danych...</div>
    <div v-else-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ errorMsg }}
    </div>

    <form v-else @submit.prevent="saveUser" class="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden">
      
      <div class="p-6 space-y-6">
        <div>
          <label class="block text-sm font-bold text-gray-700 mb-1">ID Użytkownika</label>
          <div class="text-gray-500 font-mono text-sm bg-gray-50 p-2 rounded border border-gray-200">
            {{ user.id }}
          </div>
        </div>

        <div>
          <label for="email" class="block text-sm font-bold text-gray-700 mb-1">Adres Email</label>
          <input 
            id="email" 
            v-model="formData.email" 
            type="email" 
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          >
        </div>

        <div>
          <label for="name" class="block text-sm font-bold text-gray-700 mb-1">Imię / Nazwa</label>
          <input 
            id="name" 
            v-model="formData.name" 
            type="text" 
            required
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          >
        </div>

        <div>
          <label for="role" class="block text-sm font-bold text-gray-700 mb-1">Uprawnienia (Rola)</label>
          <select 
            id="role" 
            v-model="formData.role"
            class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
          >
            <option value="USER">Zwykły użytkownik (USER)</option>
            <option value="ADMIN">Administrator (ADMIN)</option>
          </select>
        </div>

        <div class="flex items-start mt-4 pt-4 border-t border-gray-100">
          <div class="flex items-center h-5">
            <input 
              id="isVerified" 
              v-model="formData.isVerified" 
              type="checkbox" 
              class="w-4 h-4 text-green-600 border-gray-300 rounded focus:ring-green-500"
            >
          </div>
          <div class="ml-3 text-sm">
            <label for="isVerified" class="font-bold text-green-700">Konto zweryfikowane</label>
            <p class="text-gray-500">Zaznacz, jeśli użytkownik ręcznie potwierdził swój adres email.</p>
          </div>
        </div>

        <div class="flex items-start mt-4 pt-4 border-t border-gray-100">
          <div class="flex items-center h-5">
            <input 
              id="isBlocked" 
              v-model="formData.isBlocked" 
              type="checkbox" 
              class="w-4 h-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
            >
          </div>
          <div class="ml-3 text-sm">
            <label for="isBlocked" class="font-bold text-red-700">Zablokuj konto</label>
            <p class="text-gray-500">Zaznacz, aby uniemożliwić użytkownikowi logowanie się do systemu.</p>
          </div>
        </div>
      </div>

      <div class="bg-gray-50 px-6 py-4 flex items-center justify-end border-t border-gray-200 gap-3">
        <span v-if="saveSuccess" class="text-green-600 font-medium text-sm mr-auto">
          ✓ Zapisano zmiany
        </span>
        <button 
          type="button"
          @click="router.back()" 
          class="bg-white border border-gray-300 text-gray-700 px-4 py-2 rounded-lg text-sm font-medium shadow-sm hover:bg-gray-50"
        >
          Anuluj
        </button>
        <button 
          type="submit" 
          :disabled="saving"
          class="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg text-sm font-medium shadow-sm disabled:opacity-50 transition-colors"
        >
          {{ saving ? 'Zapisywanie...' : 'Zapisz' }}
        </button>
      </div>

    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userId = route.params.id

const user = ref({})
const formData = ref({
  name: '',
  email: '',
  role: 'USER',
  isVerified: false,
  isBlocked: false
})

const loading = ref(true)
const saving = ref(false)
const errorMsg = ref('')
const saveSuccess = ref(false)

const getToken = () => localStorage.getItem('trippy_token')
const getAuthHeaders = () => ({ headers: { Authorization: `Bearer ${getToken()}` } })

onMounted(async () => {
  try {
    const response = await axios.get(
      `${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${userId}`, 
      getAuthHeaders()
    )
    user.value = response.data
    
    formData.value = {
      name: user.value.name,
      email: user.value.email,
      role: user.value.role,
      isVerified: user.value.isVerified || false,
      isBlocked: user.value.isBlocked || false
    }
  } catch (error) {
    errorMsg.value = 'Nie udało się pobrać danych użytkownika.'
  } finally {
    loading.value = false
  }
})

const saveUser = async () => {
  saving.value = true
  saveSuccess.value = false
  errorMsg.value = ''

  try {
    await axios.put(
      `${import.meta.env.VITE_API_BASE_URL}/api/admin/users/${userId}`, 
      formData.value,
      getAuthHeaders()
    )
    saveSuccess.value = true
    
  } catch (error) {
    errorMsg.value = error.response?.data?.error || 'Wystąpił błąd podczas zapisywania.'
  } finally {
    saving.value = false
  }
}
</script>