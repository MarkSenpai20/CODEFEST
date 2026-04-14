package com.vantal.rvwithsql

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vantal.rvwithsql.model.DatabaseHelper
import com.vantal.rvwithsql.model.Product
import com.bumptech.glide.Glide
import com.vantal.rvwithsql.adapter.ProductAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class MainActivity : AppCompatActivity() {











    private lateinit var dbHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var listStateView: View
    private lateinit var previewStateView: View
    private lateinit var navBar: View
    private lateinit var btnHome: Button
    private lateinit var btnDashboard: Button
    private lateinit var btnSettings: Button
    private lateinit var layoutTv: TextView
    private lateinit var dashboardState: View
    private lateinit var settingState: View
    private lateinit var formState: View
    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var userEmail: TextView
    private lateinit var userPass: TextView
    private lateinit var masterLayout: View


















    // Current UI state (bitwise)
    private var currentState = UIState.FormButton.STATE_LOGIN



















    // Preview UI elements for PRODUCTS
    private lateinit var previewImage: ImageView
    private lateinit var previewName: TextView
    private lateinit var previewDesc: TextView
    private lateinit var previewPrice: TextView
    private lateinit var backButton: Button























    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)










        // Get the included views
        listStateView = findViewById(R.id.listState)
        previewStateView = findViewById(R.id.previewState)
        dashboardState = findViewById(R.id.dashboardState)
        settingState = findViewById(R.id.settingState)

        // edit text later
        layoutTv = findViewById(R.id.layout_tv)

        // Buttons for navigation
        navBar = findViewById(R.id.nav_bar)
        btnHome = navBar.findViewById(R.id.btn_goto_home)
        btnDashboard = navBar.findViewById(R.id.btn_goto_dashboard)
        btnSettings = navBar.findViewById(R.id.btn_goto_settings)

        //form login register
        formState = findViewById(R.id.form_layout)
        loginBtn = formState.findViewById(R.id.login_btn)
        registerBtn = formState.findViewById(R.id.signup_btn)
        userEmail = formState.findViewById(R.id.userEmail)
        userPass = formState.findViewById(R.id.userPass)


//        master layout
        masterLayout = findViewById(R.id.master_layout)































        // Setup RecyclerView
        recyclerView = listStateView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productAdapter = ProductAdapter(this) { product ->
            // On item click: switch to preview state and show product details
            showPreview(product)
        }

        recyclerView.adapter = productAdapter











        // Setup preview views
        previewImage = previewStateView.findViewById(R.id.previewImage)
        previewName = previewStateView.findViewById(R.id.previewName)
        previewDesc = previewStateView.findViewById(R.id.previewDesc)
        previewPrice = previewStateView.findViewById(R.id.previewPrice)
        backButton = previewStateView.findViewById(R.id.backToListButton)






        backButton.setOnClickListener {
            showListState()
        }

        dbHelper = DatabaseHelper(this)

        // Load products from DB (background thread)
        loadProducts()

        renderScreen()
    }

    fun formButtonState(view: View) {
        val email = userEmail.text.toString().trim()
        val password = userPass.text.toString().trim()

        // Validate empty fields first
        if (email.isEmpty() || password.isEmpty()) {
            // Show a Toast or error message here
            Toast.makeText(this@MainActivity, "EMPTY FIELDS", Toast.LENGTH_SHORT).show()
            return
        }

        when (view.id) {
            R.id.login_btn -> {
                val isValid = dbHelper.checkUsers(email, password)
                Toast.makeText(this@MainActivity, "$isValid", Toast.LENGTH_SHORT).show()
                if (isValid) {
                    Toast.makeText(this@MainActivity, "Login Success", Toast.LENGTH_SHORT).show()
                    // Login Success: Switch to Home/List state
                    currentState = UIState.Navigation.STATE_HOME
                    renderScreen()
                } else {
//                    userPass.error = "Invalid email or password"
                    Toast.makeText(this@MainActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.signup_btn -> {
                val id = dbHelper.addUsers(email, password)
                if (id != -1L) {
                    // Registration Success: Automatically log them in or stay on form
                    currentState = UIState.FormButton.STATE_LOGIN
                    renderScreen()
                }
            }
        }
    }

    fun onNavClick(view: View){
        currentState = when(view.id){
            R.id.btn_goto_home -> UIState.Navigation.STATE_HOME
            R.id.btn_goto_dashboard -> UIState.Navigation.STATE_DASHBOARD
            R.id.btn_goto_settings -> UIState.Navigation.STATE_SETTINGS
            else -> currentState
        }
        renderScreen()
    }


    private fun renderScreen(){

        listStateView.visibility = View.GONE
        previewStateView.visibility = View.GONE
        navBar.visibility = View.GONE
        dashboardState.visibility = View.GONE
        settingState.visibility = View.GONE
        formState.visibility = View.GONE
        masterLayout.visibility = View.GONE







        when(currentState){
            UIState.Navigation.STATE_HOME -> {
                masterLayout.visibility = View.VISIBLE
                navBar.visibility = View.VISIBLE
                currentState = UIState.NavHome.ProductList.STATE_LIST



                layoutTv.text = "HOME SCREEN"
            }
            UIState.Navigation.STATE_SETTINGS -> {
                masterLayout.visibility = View.VISIBLE
                navBar.visibility = View.VISIBLE
                settingState.visibility = View.VISIBLE


                layoutTv.text = "SETTINGS SCREEN"
            }
            UIState.Navigation.STATE_DASHBOARD -> {
                masterLayout.visibility = View.VISIBLE
                navBar.visibility = View.VISIBLE
                dashboardState.visibility = View.VISIBLE


                layoutTv.text = "DASHBOARD SCREEN"
            }

            UIState.NavHome.ProductList.STATE_PREVIEW -> {
                masterLayout.visibility = View.VISIBLE
                navBar.visibility = View.VISIBLE


                layoutTv.text = "PREVIEW SCREEN"
            }
            UIState.NavHome.ProductList.STATE_LIST -> {
                masterLayout.visibility = View.VISIBLE
                navBar.visibility = View.VISIBLE

                layoutTv.text = "HOME SCREEN"
            }
            UIState.FormButton.STATE_LOGIN -> {
                formState.visibility = View.VISIBLE

            }
            UIState.FormButton.STATE_REGISTER -> {
                formState.visibility = View.VISIBLE
            }
        }
        println(currentState.toString(16).uppercase())
            updateVisibility()
    }
















    private fun loadProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            val products = dbHelper.getAllProducts()
            withContext(Dispatchers.Main) {
                productAdapter.updateProducts(products)
            }
        }
    }

















    private fun showPreview(product: Product) {
        // Update preview UI with product data
        previewName.text = product.name
        previewDesc.text = product.description
        previewPrice.text = "$${product.price}"

        // Load image from drawable using resource name
        val resourceId = resources.getIdentifier(product.imagePath, "drawable", packageName)
        if (resourceId != 0) {
            Glide.with(this).load(resourceId).into(previewImage)
        } else {
            Glide.with(this).load(R.drawable.ic_placeholder).into(previewImage)
        }

        // Change state
        currentState = UIState.NavHome.ProductList.STATE_PREVIEW
        updateVisibility()
    }
















    private fun showListState() {
        currentState = UIState.NavHome.ProductList.STATE_LIST
        updateVisibility()
    }



















    private fun updateVisibility() {
        listStateView.visibility = if (UIState.NavHome.ProductList.isState(currentState, UIState.NavHome.ProductList.STATE_LIST)) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }

        previewStateView.visibility = if (UIState.NavHome.ProductList.isState(currentState, UIState.NavHome.ProductList.STATE_PREVIEW)) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }


























    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}