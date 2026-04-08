<template>
  <div class="space-y-6">
    
    <div class="border-b border-gray-200 pb-4">
      <h2 class="text-2xl font-bold text-gray-800">Przegląd wszystkich wyjazdów</h2>
      <p class="text-sm text-gray-500 mt-1">Widok tylko do odczytu. Podgląd aktywności użytkowników w systemie.</p>
    </div>

    <div v-if="loading" class="text-center py-10 text-gray-500">Pobieranie wyjazdów...</div>
    <div v-else-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ errorMsg }}
    </div>

    <div v-else class="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200 text-gray-600 text-xs uppercase tracking-wider">
              <th class="p-4 font-medium">ID Wyjazdu</th>
              <th class="p-4 font-medium">Nazwa (Tytuł)</th>
              <th class="p-4 font-medium">Organizator</th>
              </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 text-sm text-gray-700">
            <tr v-for="trip in trips" :key="trip.id" class="hover:bg-gray-50 transition-colors">
              <td class="p-4 font-mono text-xs text-gray-500" :title="trip.id">#{{ trip.id.split('-')[0] }}...</td>
              <td class="p-4 font-bold text-gray-900">{{ trip.title || trip.name }}</td>
              <td class="p-4 text-gray-600">{{ trip.ownerEmail || 'Brak danych' }}</td>
            </tr>
            <tr v-if="trips.length === 0">
              <td colspan="3" class="p-8 text-center text-gray-500">Brak utworzonych wyjazdów w systemie.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const trips = ref([])
const loading = ref(true)
const errorMsg = ref('')

const getToken = () => localStorage.getItem('trippy_token')

onMounted(async () => {
  try {
    const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/admin/trips`, {
      headers: { Authorization: `Bearer ${getToken()}` }
    })
    trips.value = response.data
  } catch (error) {
    errorMsg.value = 'Nie udało się pobrać listy wyjazdów.'
    console.error(error)
  } finally {
    loading.value = false
  }
})
</script>