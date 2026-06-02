package com.example.homerclicker.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

data class UserProfile(val uid: String, val username: String, val photoUrl: String)
data class LeaderboardEntry(val uid: String, val username: String, val photoUrl: String, val bestTimeMs: Long, val formattedTime: String)

object FirebaseManager {
    var isInitialized = false
        private set
        
    var currentUser by mutableStateOf<UserProfile?>(null)
        private set

    lateinit var auth: FirebaseAuth
    lateinit var database: FirebaseDatabase

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val hasDefaultApp = try {
                FirebaseApp.getInstance()
                true
            } catch (e: IllegalStateException) {
                false
            }

            if (!hasDefaultApp) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyAhpxzczueuVNJlOr8ySLaaRprJjXLPriU")
                    .setApplicationId("1:811243337021:web:6a4402334fd939c88e0154")
                    .setDatabaseUrl("https://homero-chino-clicker-default-rtdb.firebaseio.com")
                    .setProjectId("homero-chino-clicker")
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
            }
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance()
            isInitialized = true
            
            // Check current auth status
            auth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    // Fetch details from RTDB
                    database.reference.child("users").child(user.uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val name = snapshot.child("username").value as? String ?: user.displayName ?: "Jugador"
                                val photo = snapshot.child("photoUrl").value as? String ?: user.photoUrl?.toString() ?: "donut.png"
                                currentUser = UserProfile(user.uid, name, photo)
                            }
                            override fun onCancelled(error: DatabaseError) {
                                currentUser = UserProfile(user.uid, user.displayName ?: "Jugador", user.photoUrl?.toString() ?: "donut.png")
                            }
                        })
                } else {
                    currentUser = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logout() {
        if (isInitialized) {
            auth.signOut()
            currentUser = null
        }
    }

    fun loginWithGoogle(context: Context, idToken: String, onResult: (Boolean) -> Unit) {
        initialize(context)
        if (!isInitialized) {
            onResult(false)
            return
        }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userRef = database.reference.child("users").child(user.uid)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists()) {
                                    userRef.setValue(mapOf(
                                        "username" to (user.displayName ?: "Jugador"),
                                        "photoUrl" to (user.photoUrl?.toString() ?: "donut.png")
                                    )).addOnCompleteListener { dbTask ->
                                        onResult(dbTask.isSuccessful)
                                    }
                                } else {
                                    onResult(true)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                onResult(true)
                            }
                        })
                    } else {
                        onResult(false)
                    }
                } else {
                    onResult(false)
                }
            }
    }

    fun registerManual(context: Context, username: String, photoUrl: String, onResult: (Boolean) -> Unit) {
        initialize(context)
        if (!isInitialized) {
            onResult(false)
            return
        }
        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            // User already authenticated (e.g. anonymous). Just update profile in DB.
            val userRef = database.reference.child("users").child(currentUid)
            userRef.setValue(mapOf("username" to username, "photoUrl" to photoUrl))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        currentUser = UserProfile(currentUid, username, photoUrl)
                        onResult(true)
                    } else {
                        onResult(false)
                    }
                }
        } else {
            // Sign in anonymously first
            auth.signInAnonymously().addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val uid = authTask.result?.user?.uid ?: ""
                    val userRef = database.reference.child("users").child(uid)
                    userRef.setValue(mapOf("username" to username, "photoUrl" to photoUrl))
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                currentUser = UserProfile(uid, username, photoUrl)
                                onResult(true)
                            } else {
                                onResult(false)
                            }
                        }
                } else {
                    onResult(false)
                }
            }
        }
    }

    fun saveScore(context: Context, timeMs: Long, onResult: (Boolean) -> Unit) {
        initialize(context)
        val user = currentUser
        if (!isInitialized || user == null) {
            onResult(false)
            return
        }
        
        val leadRef = database.reference.child("leaderboard").child(user.uid)
        leadRef.child("bestTimeMs").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val oldTime = snapshot.value as? Long
                if (oldTime == null || timeMs < oldTime) {
                    val formatted = String.format("%.3fs", timeMs / 1000.0)
                    val data = mapOf(
                        "username" to user.username,
                        "photoUrl" to user.photoUrl,
                        "bestTimeMs" to timeMs,
                        "formattedTime" to formatted,
                        "timestamp" to System.currentTimeMillis()
                    )
                    leadRef.setValue(data).addOnCompleteListener { task ->
                        onResult(task.isSuccessful)
                    }
                } else {
                    onResult(true) // already had a better score
                }
            }
            override fun onCancelled(error: DatabaseError) {
                onResult(false)
            }
        })
    }

    fun getRanking(context: Context, onResult: (String) -> Unit) {
        initialize(context)
        val user = currentUser
        if (!isInitialized || user == null) {
            onResult("-")
            return
        }
        database.reference.child("leaderboard").orderByChild("bestTimeMs")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var rank = 1
                    var found = false
                    for (child in snapshot.children) {
                        if (child.key == user.uid) {
                            found = true
                            break
                        }
                        rank++
                    }
                    onResult(if (found) "#$rank" else "-")
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult("-")
                }
            })
    }

    fun getPersonalBest(context: Context, onResult: (Long?) -> Unit) {
        initialize(context)
        val user = currentUser
        if (!isInitialized || user == null) {
            onResult(null)
            return
        }
        database.reference.child("leaderboard").child(user.uid).child("bestTimeMs")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.value as? Long)
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(null)
                }
            })
    }

    fun fetchLeaderboard(context: Context, onResult: (List<LeaderboardEntry>) -> Unit) {
        initialize(context)
        if (!isInitialized) {
            onResult(emptyList())
            return
        }
        database.reference.child("leaderboard").orderByChild("bestTimeMs").limitToFirst(50)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val entries = mutableListOf<LeaderboardEntry>()
                    for (child in snapshot.children) {
                        val uid = child.key ?: ""
                        val name = child.child("username").value as? String ?: "Jugador"
                        val photo = child.child("photoUrl").value as? String ?: "donut.png"
                        val time = child.child("bestTimeMs").value as? Long ?: 9999999L
                        val formatted = child.child("formattedTime").value as? String ?: "-s"
                        entries.add(LeaderboardEntry(uid, name, photo, time, formatted))
                    }
                    entries.sortBy { it.bestTimeMs }
                    onResult(entries)
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }
}
