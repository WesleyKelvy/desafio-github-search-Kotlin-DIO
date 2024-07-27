package br.com.igorbag.githubsearch.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import br.com.igorbag.githubsearch.R
import br.com.igorbag.githubsearch.data.GitHubService
import br.com.igorbag.githubsearch.domain.Repository
import br.com.igorbag.githubsearch.ui.adapter.RepositoryAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var listaRepositories: RecyclerView
    lateinit var githubApi: GitHubService
    lateinit var et_nomeUsuario: EditText
    lateinit var btnConfirmar: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupView()
        setupListeners()
        setupRetrofit()
        showUserName()
//        getAllReposByUserName()
    }

    // Metodo responsavel por realizar o setup da view e recuperar os Ids do layout
    fun setupView() {
        //@TODO 1 - Recuperar os Id's da tela para a Activity com o findViewById -- OK!
        listaRepositories = findViewById(R.id.rv_lista_repositories)
        et_nomeUsuario = findViewById(R.id.et_nome_usuario)
        btnConfirmar = findViewById(R.id.btn_confirmar)
    }

    //metodo responsavel por configurar os listeners click da tela
    private fun setupListeners() {
        //@TODO 2 - colocar a acao de click do botao confirmar -- OK!

        btnConfirmar.setOnClickListener {
            val userName = et_nomeUsuario.text.toString()
            saveUserLocal(userName)
            getAllReposByUserName(userName)
            Log.d("CLicked--OK!!", "")
        }
    }

    // salvar o usuario preenchido no EditText utilizando uma SharedPreferences
    private fun saveUserLocal(userName: String) {
        //@TODO 3 - Persistir o usuario preenchido na editText com a SharedPref no listener do botao salvar -- OK!

        val sharedPreferences =
            getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putString("userName", userName)
        editor.apply()
    }

    private fun showUserName() {
        //@TODO 4- depois de persistir o usuario exibir sempre as informacoes no EditText  se a sharedpref possuir algum valor, exibir no proprio editText o valor salvo -- OK!
        val sharedPreferences =
            getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedUser = sharedPreferences.getString(
            "userName",
            ""
        ) //defValue-> value for empty sharedpref state.

        savedUser?.let {
            if (savedUser != "") {
                getAllReposByUserName(savedUser)
            }
        }
        et_nomeUsuario.setText(savedUser)
    }

    //Metodo responsavel por fazer a configuracao base do Retrofit
    fun setupRetrofit() {/*
           @TODO 5 -  realizar a Configuracao base do retrofit -- OK!
           Documentacao oficial do retrofit - https://square.github.io/retrofit/
        */

        val buider = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        githubApi = buider.create(GitHubService::class.java)
    }

    //Metodo responsavel por buscar todos os repositorios do usuario fornecido
    fun getAllReposByUserName(user: String) {
        // TODO 6 - realizar a implementacao do callback do retrofit e chamar o metodo setupAdapter se retornar os dados com sucesso -- OK!
        try {
            githubApi.getAllRepositoriesByUser(user)
                .enqueue(object : Callback<List<Repository>> {
                    override fun onResponse(
                        call: Call<List<Repository>>,
                        response: Response<List<Repository>>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let {
                                Log.d(
                                    "Starting in getAll->",
                                    it.toString()
                                )

                                setupAdapter(it)
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<List<Repository>>,
                        t: Throwable
                    ) {
                    }
                })

        } catch (error: Exception) {
            Log.e("Error in getAllRepos-->", error.toString())
            Toast.makeText(
                this,
                "Usuário não encontrado.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Metodo responsavel por realizar a configuracao do adapter
    fun setupAdapter(list: List<Repository>) {/*
            @TODO 7 - Implementar a configuracao do Adapter , construir o adapter e instancia-lo -- OK!
            passando a listagem dos repositorios
         */
        val adapter = RepositoryAdapter(list)

        adapter.repoItemLister =
            { repository -> openBrowser(repository.htmlUrl) }

        adapter.btnShareLister = { repository ->
            shareRepositoryLink(repository.htmlUrl)
        }

        listaRepositories.adapter = adapter

    }


    // Metodo responsavel por compartilhar o link do repositorio selecionado
    // @Todo 11 - Colocar esse metodo no click do share item do adapter -- OK!
    fun shareRepositoryLink(urlRepository: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, urlRepository)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    // Metodo responsavel por abrir o browser com o link informado do repositorio

    // @Todo 12 - Colocar esse metodo no click item do adapter -- OK!
    fun openBrowser(urlRepository: String) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW, Uri.parse(urlRepository)
            )
        )

    }

}