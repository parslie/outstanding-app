package com.tavro.parslie.outstanding.ui.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.tavro.parslie.outstanding.BuildConfig
import com.tavro.parslie.outstanding.R
import com.tavro.parslie.outstanding.data.repository.PreferenceRepository
import com.tavro.parslie.outstanding.databinding.ActivityLoginBinding
import com.tavro.parslie.outstanding.ui.viewmodel.ContextualViewModelFactory
import com.tavro.parslie.outstanding.ui.viewmodel.LoginViewModel
import com.tavro.parslie.outstanding.util.Status

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.loginVersion.text = getString(R.string.app_version, BuildConfig.VERSION_NAME)

        initViewModel()
        initListeners()
    }

    private fun initViewModel() {
        val viewModelFactory = ContextualViewModelFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        viewModel.getLoginResponse().observe(this, {
            when(it.status) {
                Status.LOADING -> {
                    binding.loginProgressBar.visibility = View.VISIBLE
                    binding.loginLoginBtn.isEnabled = false
                }
                Status.SUCCESS -> {
                    val prefs = PreferenceRepository(applicationContext)
                    prefs.authToken = it.data!!.token

                    // TODO: go to main activity
                    finish()
                }
                Status.ERROR -> {
                    binding.loginProgressBar.visibility = View.INVISIBLE
                    binding.loginLoginBtn.isEnabled = true
                    Toast.makeText(applicationContext, "error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun initListeners() {
        binding.loginLoginBtn.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()
            viewModel.login(email, password)
        }
    }
}