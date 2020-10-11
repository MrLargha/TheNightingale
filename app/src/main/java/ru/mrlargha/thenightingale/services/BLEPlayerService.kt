package ru.mrlargha.thenightingale.services

import android.app.Service
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.STATE_CONNECTING
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log


class BLEPlayerService : Service() {

    companion object {
        val TAG = BLEPlayerService::class.simpleName
    }

    private var mConnectionState: Int = STATE_DISCONNECTED
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothManager: BluetoothManager? = null
    var mBluetoothAdapter: BluetoothAdapter? = null

    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                intentAction = ACTION_GATT_CONNECTED
//                mConnectionState = STATE_CONNECTED
//                broadcastUpdate(intentAction)
//                Log.i(TAG, "Connected to GATT server.")
//                // Attempts to discover services after successful connection.
//                Log.i(
//                    TAG, "Attempting to start service discovery:" +
//                            mBluetoothGatt.discoverServices()
//                )
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                intentAction = ACTION_GATT_DISCONNECTED
//                mConnectionState = STATE_DISCONNECTED
//                Log.i(TAG, "Disconnected from GATT server.")
//                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    fun initialize(): Boolean {
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mBluetoothManager ?: run {
                Log.e(TAG, "Unable to initialize BT service")
                return false
            }
        }
        mBluetoothAdapter = mBluetoothManager?.adapter
        mBluetoothAdapter ?: run {
            Log.e(TAG, "Unable to obtain BT Adapter")
            return false
        }

        return true
    }

    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            return if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                true
            } else {
                false
            }
        }
        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt?.disconnect()
    }

    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return mBluetoothGatt?.services
    }

    fun close() {
        mBluetoothGatt?.close()
        mBluetoothGatt = null
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBind
    }

    override fun onCreate() {
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    inner class BLEPlayerServiceBinder : Binder() {
        fun getService(): BLEPlayerService {
            return this@BLEPlayerService
        }
    }

    private val mBind = BLEPlayerServiceBinder()
}