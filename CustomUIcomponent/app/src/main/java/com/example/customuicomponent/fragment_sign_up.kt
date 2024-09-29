package com.example.customuicomponent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

data class SignUpRequest(
    val user: String,
    val email: String,
    val password: String
)

class UserSignUpFragment : Fragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginTextView: TextView
    private lateinit var errorTextView: TextView

    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usernameEditText = view.findViewById(R.id.usernameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        signUpButton = view.findViewById(R.id.signUpButton)
        loginTextView = view.findViewById(R.id.loginTextView)
        errorTextView = view.findViewById(R.id.errorTextView)

        signUpButton.setOnClickListener {
            performSignUp()
        }

        loginTextView.setOnClickListener {
            // Switch to Login Fragment
            (activity as MainActivity).setFragment(UserLoginFragment())
        }
    }

    private fun performSignUp() {
        val username = usernameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // Check if inputs are not empty
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if passwords match
        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        // Prepare the JSON object to be sent to the server
        val jsonObject = JSONObject()
        jsonObject.put("user", username)
        jsonObject.put("email", email)
        jsonObject.put("password", password)

        // Convert JSON to string and set content type
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaType())

        // Build the request
        val request = Request.Builder()
            .url("https://flask.cijenolovac.indigoingenium.ba/signup")
            .post(requestBody)
            .build()

        // Execute the request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle network failure
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val jsonResponse = JSONObject(responseData)

                activity?.runOnUiThread {
                    if (jsonResponse.has("error")) {
                        // Show error in errorTextView
                        errorTextView.text = jsonResponse.getString("error")
                        errorTextView.visibility = View.VISIBLE
                    } else if (jsonResponse.has("message")) {
                        // Show success and navigate to login
                        Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                        (activity as MainActivity).setFragment(UserLoginFragment())
                    }
                }
            }
        })
    }
}
