package com.example.customuicomponent

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ScannedReceipt.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScannedReceipt : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var scanQrBtn: Button
    private lateinit var scannedValueTv: TextView
    private var isScannerInstalled = false
    private lateinit var scanner: GmsBarcodeScanner

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_scanned_receipt, container, false)

        installGoogleScanner()

        scanQrBtn = rootView.findViewById(R.id.scanQrBtn)

        val options = initializeGoogleScanner()
        scanner = GmsBarcodeScanning.getClient(requireContext(), options)

        registerUiListener()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        // Retrieve the user_id
        val userId = sharedPreferences.getInt("user_id", -1)  // Default to -1 if not found
        if (userId != -1) {
            // Use the user_id as needed
            Toast.makeText(requireContext(), "User ID: $userId", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ScannedReceipt.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ScannedReceipt().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun installGoogleScanner() {
        val moduleInstall = ModuleInstall.getClient(requireContext())
        val moduleInstallRequest = ModuleInstallRequest.newBuilder()
            .addApi(GmsBarcodeScanning.getClient(requireContext()))
            .build()

        moduleInstall.installModules(moduleInstallRequest).addOnSuccessListener {
            isScannerInstalled = true
        }.addOnFailureListener {
            isScannerInstalled = false
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeGoogleScanner(): GmsBarcodeScannerOptions {
        return GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom().build()
    }

    private fun registerUiListener() {
        scanQrBtn.setOnClickListener {
            if (isScannerInstalled) {
                startScanning()
            } else {
                Toast.makeText(requireContext(), "Please try again...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startScanning() {
        scanner.startScan().addOnSuccessListener {
            val result = it.rawValue
            result?.let {
                if (Patterns.WEB_URL.matcher(it).matches()) {
//                    Toast.makeText(requireContext(), "URL ispravan", Toast.LENGTH_SHORT).show()
                    sendUrlToServer(it)
                } else {
                    Toast.makeText(requireContext(), "Neispravan URL", Toast.LENGTH_SHORT).show()
                }
//                scannedValueTv.text = buildString {
//                    append("Scanned Value : ")
//                    append(it)
//                }
            }
        }.addOnCanceledListener {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()

        }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()

            }
    }

    private fun sendUrlToServer(scannedValue: String) {
        val url = "https://flask.cijenolovac.indigoingenium.ba/scrape"

        // Create JSON body
        val sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("user_id", -1)

        // Create JSON body and include user_id
        val jsonBody = JSONObject().apply {
            put("url", scannedValue)
            put("user_id", userId)  // Add user_id to JSON object
        }.toString()

        val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())

        // Create HTTP client
        val client = OkHttpClient()

        // Make network call in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Build the request
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                Log.d("JSONBody", jsonBody)

                // Execute the request
                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    // Parse the response
                    val responseBody = response.body?.string()
                    Log.d("Response", responseBody ?: "No response body")

                    withContext(Dispatchers.Main) {
                        // Update the UI with the response from the server
                        val fragmentView = requireView()
                        try {
                            // Assuming responseBody is in JSON format
                            val jsonObject = JSONObject(responseBody)

                            // Check if it contains an "error" attribute
                            if (jsonObject.has("error")) {
                                // If there's an error, show an error message or handle it accordingly
                                val errorMessage = jsonObject.getString("error")
                                Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_SHORT).show()
                            } else {
                                // No error, proceed with updating the UI
                                updateReceiptUI(fragmentView, responseBody ?: "No response")
                            }
                        } catch (e: JSONException) {
                            // In case of invalid JSON or other issues, show a generic error or handle accordingly
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("ErrorResponse", "Error: ${response.code}")
                    Log.e("ErrorBody", response.body?.string() ?: "No response body")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Request failed", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Network Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun updateReceiptUI(view: View, jsonResponse: String) {
        // Parse the JSON response
        val jsonObject = JSONObject(jsonResponse)

        // Get static data from JSON
        val storeName = jsonObject.getString("store_name")
        val storeAddress = jsonObject.getString("store_address")
        val cashierName = jsonObject.getString("cashier_name")
        val receiptDate = jsonObject.getString("receipt_issue_date")
        val receiptTotal = jsonObject.getString("receipt_total")

        // Find TextViews and update them with the JSON data
        val storeNameTv: TextView = view.findViewById(R.id.storeNameTv)
        val storeAddressTv: TextView = view.findViewById(R.id.storeAddressTv)
        val cashierNameTv: TextView = view.findViewById(R.id.cashierNameTv)
        val receiptDateTv: TextView = view.findViewById(R.id.receiptDateTv)
        val receiptTotalTv: TextView = view.findViewById(R.id.receiptTotalTv)

        storeNameTv.text = storeName
        storeAddressTv.text = storeAddress
        cashierNameTv.text = cashierName
        receiptDateTv.text = receiptDate
        receiptTotalTv.text = "Total: $receiptTotal"

        // Parse item list from JSON
        val itemList = mutableListOf<Item>()
        val itemsArray = jsonObject.getJSONArray("item_list")
        for (i in 0 until itemsArray.length()) {
            val itemObj = itemsArray.getJSONObject(i)
            val itemName = itemObj.getString("item_store_name")
            val itemPrice = itemObj.getString("price")
            val itemQuantity = itemObj.getString("quantity")
            itemList.add(Item(itemName, itemPrice, itemQuantity))
        }

        // Set RecyclerView adapter for item list
        val itemRecyclerView: RecyclerView = view.findViewById(R.id.itemRecyclerView)
        val itemAdapter = ItemAdapter(itemList)
        itemRecyclerView.layoutManager = LinearLayoutManager(view.context)
        itemRecyclerView.adapter = itemAdapter
    }
}

data class Item(
    val itemStoreName: String,
    val price: String,
    val quantity: String
)

class ItemAdapter(private val items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.itemNameTv)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPriceTv)
        val itemQuantity: TextView = itemView.findViewById(R.id.itemQuantityTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.itemStoreName
        holder.itemPrice.text = item.price
        holder.itemQuantity.text = item.quantity
    }

    override fun getItemCount(): Int {
        return items.size
    }
}