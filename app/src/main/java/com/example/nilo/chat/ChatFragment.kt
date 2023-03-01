package com.example.nilo.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nilo.Constants
import com.example.nilo.R
import com.example.nilo.databinding.FragmentChatBinding
import com.example.nilo.entities.Message
import com.example.nilo.entities.Order
import com.example.nilo.order.OrderAux
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


/**
 * Proyect: Nilo
 * From: com.example.nilo.chat
 * Create by Pedro Aguilar Fernández on 02/01/2023 at 18:13
 * More info: linkedin.com/in/pedro-aguilar-fernández-167753140
 * All rights reserved 2023
 **/
class ChatFragment :  Fragment(), OnChatListener{

    private var binding: FragmentChatBinding? = null
    private lateinit var adapter: ChatAdapter
    private var order: Order? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        binding?.let {
            return it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getOrder()
        setupRecyclerView()
        setupButtoms()
    }

    private fun getOrder() {
        (activity as? OrderAux)?.getOrderSelected()
        order?.let {
            setupActionBar()
            setupRealtimeDatabase()
        }
    }

    private fun setupRealtimeDatabase() {
      order?.let {
          //instanciamos la bd
          val database = Firebase.database
          //hacemos la referencia a la carpeta en la bd. Con esto crearemos un chat para cada pedido
          val chatRef = database.getReference(Constants.PATH_CHATS).child(it.id)

          val childListener = object : ChildEventListener{
              override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                  val message = snapshot.getValue(Message::class.java)
//                  //configuramos el id del mensaje
//                  message?.let { message ->
//                      snapshot.key?.let {
//                          message.id = it
//                      }
//                      //asignamos el UserId
//                      FirebaseAuth.getInstance().currentUser?.let { user ->
//                          message.myUid = user.uid
//                      }
//                      //agregamos el mensaje ya configurado al adapter
//                      adapter.add(message)
//                      //para que se recorra el scroll hacia la ultima posicion
//                      binding?.recyclerView?.scrollToPosition(adapter.itemCount - 1)
//                  }
                  getMessage(snapshot)?.let {
                      //agregamos el mensaje ya configurado al adapter
                      adapter.add(it)
                      //para que se recorra el scroll hacia la ultima posicion
                      binding?.recyclerView?.scrollToPosition(adapter.itemCount - 1)
                  }

              }

              override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                  val message = snapshot.getValue(Message::class.java)
//                  //configuramos el id del mensaje
//                  message?.let { message ->
//                      snapshot.key?.let {
//                          message.id = it
//                      }
//                      //asignamos el UserId
//                      FirebaseAuth.getInstance().currentUser?.let { user ->
//                          message.myUid = user.uid
//                      }
//                      //agregamos el mensaje ya configurado al adapter
//                      adapter.update(message)
//                  }
                  getMessage(snapshot)?.let {
                      //agregamos el mensaje ya configurado al adapter
                      adapter.update(it)
                  }
              }

              override fun onChildRemoved(snapshot: DataSnapshot) {
//                  val message = snapshot.getValue(Message::class.java)
//                  //configuramos el id del mensaje
//                  message?.let { message ->
//                      snapshot.key?.let {
//                          message.id = it
//                      }
//                      //asignamos el UserId
//                      FirebaseAuth.getInstance().currentUser?.let { user ->
//                          message.myUid = user.uid
//                      }
//                  }
                  getMessage(snapshot)?.let {
                      //agregamos el mensaje ya configurado al adapter
                      adapter.delete(it)
                  }
              }

              override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

              override fun onCancelled(error: DatabaseError) {
                  binding?.let {
                      Snackbar.make(it.root, "Error al cargar el chat.", Snackbar.LENGTH_LONG).show()
                  }
              }
          }
          chatRef.addChildEventListener(childListener)
      }
    }

    private fun getMessage(snapshot: DataSnapshot) : Message? {
        snapshot.getValue(Message::class.java)?.let { message ->
        //configuramos el id del mensaje
            snapshot.key?.let {
                message.id = it
            }
            //asignamos el UserId
            FirebaseAuth.getInstance().currentUser?.let { user ->
                message.myUid = user.uid
            }
            return message
        }
        return null
    }

    private fun setupRecyclerView(){
        adapter = ChatAdapter(mutableListOf(), this)
        binding?.let {
            it.recyclerView.apply {
                layoutManager = LinearLayoutManager(context).also {
                    //".also y stackFromEnd" es para que el chat se empiece a visualizar de abajo a arriba
                    it.stackFromEnd = true
                }
                adapter = this@ChatFragment.adapter
            }
        }

//        (1..20).forEach {
//            adapter.add(Message(it.toString(), if (it%4 == 0)", Hola, ¿Como estas?, Hola, ¿Como estas?" else "Hola, ¿Como estas?",
//                if (it%3 == 0)"tu" else "yo", "yo"))
//        }
    }

    private fun setupButtoms(){
        binding?.let { binding ->
            binding.ibSend.setOnClickListener {
                sendMessage()
            }
        }
    }

    private fun sendMessage() {
        binding?.let { binding ->
            order?.let {
                //instanciamos la bd
                val database = Firebase.database
                //hacemos la referencia a la carpeta en la bd. Con esto crearemos un chat para cada pedido
                val chatRef = database.getReference(Constants.PATH_CHATS).child(it.id)
                //hay que extraer nuestro userID
                val user  = FirebaseAuth.getInstance().currentUser
                user?.let {
                    val message = Message(message = binding.etMessage.text.toString().trim(),
                        sender = it.uid)
                    //en caso de que esto se cumpla podemos inhabilitar el boton enviar y asi evitar que se envien varios msg
                    binding.ibSend.isEnabled = false

                    chatRef.push().setValue(message)
                        .addOnSuccessListener {
                            binding.etMessage.setText("")
                        }
                        .addOnCompleteListener {
                            binding.ibSend.isEnabled = true
                        }
                }
            }
        }



    }

    private fun setupActionBar(){
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            it.supportActionBar?.title = getString(R.string.chat_title)
            setHasOptionsMenu(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home){
            activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Liberamos a binding
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDestroy() {
        (activity as? AppCompatActivity)?.let {
            it.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            it.supportActionBar?.title = getString(R.string.order_title)
            setHasOptionsMenu(false)
        }
        super.onDestroy()
    }

    override fun deleteMessage(message: Message) {
        order?.let {
            val database = Firebase.database
            val messageRef = database.getReference(Constants.PATH_CHATS).child(it.id).child(message.id)
            messageRef.removeValue { error, ref ->
              binding?.let {
                  if (error != null){
                      Snackbar.make(it.root, "Error al borrar mensaje.", Snackbar.LENGTH_LONG).show()
                  }else{
                      Snackbar.make(it.root, "Mensaje borrado.", Snackbar.LENGTH_LONG).show()
                  }
              }
            }
        }
    }
}