package com.example.customuicomponent

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class UserLoginFragment : Fragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var rememberMeCheckBox: CheckBox
    private lateinit var signUpTextView: TextView
    private lateinit var errorTextView: TextView

    // SharedPreferences for storing the username
    private lateinit var sharedPreferences: SharedPreferences

    private val BASE_URL = "https://flask.cijenolovac.indigoingenium.ba/login"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameEditText = view.findViewById(R.id.usernameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        rememberMeCheckBox = view.findViewById(R.id.rememberMeCheckBox)
        signUpTextView = view.findViewById(R.id.signUpTextView)
        errorTextView = view.findViewById(R.id.errorTextView)

        sharedPreferences =
            requireActivity().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        val savedUsername = sharedPreferences.getString("username", "")
        val savedPassword = sharedPreferences.getString("password", "")

        if (!savedUsername.isNullOrEmpty()) {
            usernameEditText.setText(savedUsername)
            rememberMeCheckBox.isChecked = true
        }

        if (!savedPassword.isNullOrEmpty()) {
            passwordEditText.setText(savedPassword)
        }

        loginButton.setOnClickListener {
            performLogin()
        }

        signUpTextView.setOnClickListener {
            // Switch to UserSignUpFragment
            (activity as MainActivity).setFragment(UserSignUpFragment())
        }
    }

    private fun performLogin() {
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        } else {
            val loginRequest = LoginRequest(username, "user@example.com", password)
            sendLoginRequest(loginRequest)
        }
    }

    private fun sendLoginRequest(loginRequest: LoginRequest) {
        val client = OkHttpClient()
        val gson = Gson()

        // Convert the login request object to JSON
        val requestBody = gson.toJson(loginRequest)

        // Create the request body for OkHttp
        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            requestBody
        )

        // Build the POST request
        val request = Request.Builder()
            .url(BASE_URL)
            .post(body)
            .build()

        // Make the HTTP request asynchronously
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    displayErrorMessage("Network error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                // Parse the response JSON
                if (responseBody != null) {
                    val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)

                    requireActivity().runOnUiThread {
                        if (response.isSuccessful) {
                            if (loginResponse.message == "Login successful") {
                                // Save user_id to SharedPreferences
                                with(sharedPreferences.edit()) {
                                    putInt("user_id", loginResponse.user_id ?: -1)  // Use default value if user_id is null
                                    apply()
                                }

                                // Save credentials if "Remember Me" is checked
                                if (rememberMeCheckBox.isChecked) {
                                    with(sharedPreferences.edit()) {
                                        putString("username", loginRequest.user)
                                        putString("password", loginRequest.password)
                                        apply()
                                    }
                                }

                                // Navigate to the next fragment
                                (activity as MainActivity).setFragment(ScannedReceipt())
                            } else {
                                displayErrorMessage(loginResponse.error ?: "Unknown error")
                            }
                        } else {
                            displayErrorMessage(loginResponse.error ?: "Invalid login response")
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        displayErrorMessage("Empty response from server")
                    }
                }
            }
        })
    }

    private fun displayErrorMessage(message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }

    data class LoginRequest(val user: String, val email: String, val password: String)
    data class LoginResponse(val message: String?, val error: String?, val user_id: Int?)
}
