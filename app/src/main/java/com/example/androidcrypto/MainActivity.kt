package com.example.androidcrypto

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.dataStore
import com.example.androidcrypto.data.CryptoManager
import com.example.androidcrypto.data.UserSettings
import com.example.androidcrypto.data.UserSettingsSerializer
import com.example.androidcrypto.ui.theme.AndroidCryptoTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    private val Context.dataStore by dataStore(
        fileName = "user-settings.json",
        serializer = UserSettingsSerializer(CryptoManager())
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidCryptoTheme {
                var username by remember {
                    mutableStateOf("")
                }
                var password by remember {
                    mutableStateOf("")
                }
                var settings by remember {
                    mutableStateOf(UserSettings())
                }
                val scope = rememberCoroutineScope()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "Username") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(text = "Password") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            scope.launch {
                                dataStore.updateData {
                                    UserSettings(
                                        username = username,
                                        password = password
                                    )
                                }
                            }
                        }) {
                            Text(text = "Save")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            scope.launch {
                                settings = dataStore.data.first()
                            }
                        }) {
                            Text(text = "Load")
                        }
                    }
                    Text(text = settings.toString())
                }
            }
        }
    }
}

@Composable
fun CryptoExample(filesDir: File) {
    val cryptoManager = CryptoManager()
    AndroidCryptoTheme {
        var messageToEncrypt by remember { mutableStateOf("") }
        var messageToDecrypt by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            TextField(
                value = messageToEncrypt,
                onValueChange = { messageToEncrypt = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = "Encrypt string") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = {
                    val bytes = messageToEncrypt.encodeToByteArray()
                    val file = File(filesDir, "secret.txt")
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    val fos = FileOutputStream(file)
                    messageToDecrypt = cryptoManager.encrypt(
                        bytes = bytes,
                        outputStream = fos
                    ).decodeToString()
                }) {
                    Text(text = "Encrypt")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    val file = File(filesDir, "secret.txt")
                    messageToEncrypt = cryptoManager.decrypt(
                        inputStream = FileInputStream(file)
                    ).decodeToString()
                }) {
                    Text(text = "Decrypt")
                }
            }
            Text(text = messageToDecrypt)
        }
    }
}