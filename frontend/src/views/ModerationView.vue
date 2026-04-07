<template>
  <div class="space-y-6">
    
    <div class="border-b border-gray-200 pb-4 flex justify-between items-end">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">Moderacja treści</h2>
        <p class="text-sm text-gray-500 mt-1">Zarządzaj wpisami i zdjęciami naruszającymi regulamin.</p>
      </div>
      
      <div class="flex space-x-1 bg-gray-100 p-1 rounded-lg">
        <button 
          @click="activeTab = 'posts'" 
          :class="['px-4 py-2 rounded-md text-sm font-medium transition-colors', activeTab === 'posts' ? 'bg-white shadow-sm text-indigo-700' : 'text-gray-600 hover:text-gray-900']"
        >
          Wpisy (Posty)
        </button>
        <button 
          @click="activeTab = 'photos'" 
          :class="['px-4 py-2 rounded-md text-sm font-medium transition-colors', activeTab === 'photos' ? 'bg-white shadow-sm text-indigo-700' : 'text-gray-600 hover:text-gray-900']"
        >
          Zdjęcia
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-10 text-gray-500">Ładowanie treści...</div>
    <div v-else-if="errorMsg" class="bg-red-50 text-red-600 p-4 rounded-lg border border-red-200">
      {{ errorMsg }}
    </div>

    <div v-else>
      <div v-if="activeTab === 'posts'" class="bg-white shadow-sm rounded-lg border border-gray-200 overflow-hidden">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-50 border-b border-gray-200 text-gray-600 text-xs uppercase tracking-wider">
              <th class="p-4 font-medium">Autor</th>
              <th class="p-4 font-medium w-1/2">Treść posta</th>
              <th class="p-4 font-medium text-right">Akcje</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200 text-sm text-gray-700">
            <tr v-for="post in posts" :key="post.id" class="hover:bg-gray-50">
              <td class="p-4 font-medium text-gray-900">{{ post.authorName || 'Nieznany' }}</td>
              <td class="p-4 text-gray-600 italic">"{{ post.content }}"</td>
              <td class="p-4 text-right">
                <button @click="deleteContent('posts', post.id)" class="text-red-600 hover:text-red-900 font-medium px-2 py-1 bg-red-50 hover:bg-red-100 rounded transition-colors">
                  Usuń trwale
                </button>
              </td>
            </tr>
            <tr v-if="posts.length === 0">
              <td colspan="3" class="p-8 text-center text-gray-500">Brak postów do moderacji.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="activeTab === 'photos'" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        <div v-for="photo in photos" :key="photo.id" class="bg-white border border-gray-200 rounded-lg overflow-hidden shadow-sm group relative">
          <img :src="photo.url || 'https://via.placeholder.com/300?text=Brak+Podglądu'" class="w-full h-48 object-cover" />
          
          <div class="p-3 border-t border-gray-100 bg-gray-50 flex justify-between items-center">
            <span class="text-xs text-gray-500 truncate" :title="photo.authorName">Od: {{ photo.authorName || 'Ktoś' }}</span>
            <button @click="deleteContent('photos', photo.id)" class="text-xs bg-red-100 text-red-700 hover:bg-red-600 hover:text-white px-2 py-1 rounded transition-colors font-bold">
              USUŃ
            </button>
          </div>
        </div>
        <div v-if="photos.length === 0" class="col-span-full py-10 text-center text-gray-500 border-2 border-dashed border-gray-200 rounded-lg">
          Brak zdjęć w systemie.
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'

const activeTab = ref('posts') // Domyślna zakładka
const posts = ref([])
const photos = ref([])
const loading = ref(false)
const errorMsg = ref('')

const getToken = () => localStorage.getItem('trippy_token')
const getAuthHeaders = () => ({ headers: { Authorization: `Bearer ${getToken()}` } })

const fetchData = async () => {
  loading.value = true
  errorMsg.value = ''
  try {
    if (activeTab.value === 'posts') {
      const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/admin/moderation/posts`, getAuthHeaders())
      posts.value = response.data
    } else {
      const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/api/admin/moderation/photos`, getAuthHeaders())
      photos.value = response.data
    }
  } catch (error) {
    errorMsg.value = `Nie udało się pobrać danych dla zakładki: ${activeTab.value}`
  } finally {
    loading.value = false
  }
}

watch(activeTab, () => {
  fetchData()
})

onMounted(() => {
  fetchData()
})

const deleteContent = async (type, id) => {
  const isPost = type === 'posts'
  const itemName = isPost ? 'ten post' : 'to zdjęcie'
  
  if (!window.confirm(`⚠️ Czy na pewno chcesz trwale usunąć ${itemName}? Tej akcji nie można cofnąć.`)) {
    return
  }

  try {
    await axios.delete(`${import.meta.env.VITE_API_BASE_URL}/api/admin/moderation/${type}/${id}`, getAuthHeaders())
    
    // Usuń element z odpowiedniej tablicy (reaktywność UI)
    if (isPost) {
      posts.value = posts.value.filter(p => p.id !== id)
    } else {
      photos.value = photos.value.filter(p => p.id !== id)
    }
  } catch (error) {
    alert('Błąd podczas usuwania. Odśwież stronę i spróbuj ponownie.')
  }
}
</script>