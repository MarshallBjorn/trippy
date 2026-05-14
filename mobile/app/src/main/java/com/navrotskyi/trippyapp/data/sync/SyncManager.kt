package com.navrotskyi.trippyapp.data.sync

import android.content.Context
import com.google.gson.Gson
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.data.dao.PendingOperationDao
import com.navrotskyi.trippyapp.data.database.TrippyDb
import com.navrotskyi.trippyapp.data.entity.PendingOperationEntity
import com.navrotskyi.trippyapp.data.network.NetworkMonitor
import com.navrotskyi.trippyapp.data.repository.TripRepository
import com.navrotskyi.trippyapp.models.CreateTripEventRequest
import com.navrotskyi.trippyapp.models.CreateTripNodeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

object SyncManager {

    enum class OpType { CREATE_TRIP, CREATE_NODE, UPDATE_NODE, DELETE_NODE }

    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var api: TrippyApi
    private lateinit var pendingDao: PendingOperationDao
    private lateinit var repo: TripRepository

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    fun init(context: Context) {
        val db = TrippyDb.getInstance(context)
        api = RetrofitClient.retrofit.create(TrippyApi::class.java)
        pendingDao = db.pendingOperationDao()
        repo = TripRepository(api, db.tripDao(), db.tripNodeDao())

        pendingDao.observeCount()
            .onEach { _pendingCount.value = it }
            .launchIn(scope)

        NetworkMonitor.isOnline
            .distinctUntilChanged()
            .onEach { online ->
                if (online) syncNow()
            }
            .launchIn(scope)
    }

    suspend fun syncNow() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        try {
            drainPendingQueue()
            repo.refreshTrips()
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun drainPendingQueue() {
        val pending = pendingDao.getAll()
        for (op in pending) {
            val ok = executeOperation(op)
            if (ok) {
                pendingDao.delete(op)
            } else {
                pendingDao.update(op.copy(retryCount = op.retryCount + 1))
                // jeśli zbyt wiele prób — usuń (zapobiega zapętleniu)
                if (op.retryCount + 1 >= 5) pendingDao.delete(op)
            }
        }
    }

    private suspend fun executeOperation(op: PendingOperationEntity): Boolean {
        return try {
            when (OpType.valueOf(op.type)) {
                OpType.CREATE_TRIP -> {
                    val req = gson.fromJson(op.payload, CreateTripEventRequest::class.java)
                    api.createTrip(req).isSuccessful
                }
                OpType.CREATE_NODE -> {
                    val req = gson.fromJson(op.payload, CreateTripNodeRequest::class.java)
                    api.createTripNode(op.tripId!!, req).isSuccessful
                }
                OpType.UPDATE_NODE -> {
                    val req = gson.fromJson(op.payload, CreateTripNodeRequest::class.java)
                    api.updateTripNode(op.tripId!!, op.nodeId!!, req).isSuccessful
                }
                OpType.DELETE_NODE -> {
                    api.deleteTripNode(op.tripId!!, op.nodeId!!).isSuccessful
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun enqueueCreateTrip(req: CreateTripEventRequest) {
        pendingDao.insert(
            PendingOperationEntity(
                type = OpType.CREATE_TRIP.name,
                payload = gson.toJson(req)
            )
        )
    }

    suspend fun enqueueCreateNode(tripId: String, req: CreateTripNodeRequest) {
        pendingDao.insert(
            PendingOperationEntity(
                type = OpType.CREATE_NODE.name,
                tripId = tripId,
                payload = gson.toJson(req)
            )
        )
    }

    suspend fun enqueueUpdateNode(tripId: String, nodeId: String, req: CreateTripNodeRequest) {
        pendingDao.insert(
            PendingOperationEntity(
                type = OpType.UPDATE_NODE.name,
                tripId = tripId,
                nodeId = nodeId,
                payload = gson.toJson(req)
            )
        )
    }

    suspend fun enqueueDeleteNode(tripId: String, nodeId: String) {
        pendingDao.insert(
            PendingOperationEntity(
                type = OpType.DELETE_NODE.name,
                tripId = tripId,
                nodeId = nodeId,
                payload = "{}"
            )
        )
    }
}