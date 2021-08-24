package com.example.wallettest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.wallettest.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.crypto.*

import org.web3j.protocol.http.HttpService

import org.web3j.protocol.Web3j
import java.util.concurrent.ExecutionException
import java.lang.Exception

import timber.log.Timber
import java.math.BigInteger
import java.security.SecureRandom
import org.web3j.crypto.CipherException

import org.web3j.crypto.Wallet

import org.web3j.crypto.Keys

import org.web3j.crypto.ECKeyPair

import org.json.JSONObject
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException


class MainActivity : AppCompatActivity() {

    private val web3: Web3j =
        Web3j.build(HttpService("https://fragrant-aged-wood.ropsten.quiknode.pro/bd3b90c5a36a4705dc374f9a7f4e00342926fedc/"))

    private val keyPair: ECKeyPair by lazy {
        Keys.createEcKeyPair()
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        CoroutineScope(Dispatchers.IO).launch {
            try {
                Timber.d("Version = ${web3.getClientVersion()}")
                Timber.d("BlockNumber = ${web3.getBlockNumber()}")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnGenerateMnemonic.setOnClickListener {
            val mnemonic = getMnemonic(32)
            Timber.d("Mnemonic = $mnemonic")
            binding.tvMnemonic.text = mnemonic

            val seed = mnemonic.generateSeed().toHex()
            Timber.d("Seed = $seed")
            binding.tvSeed.text = seed


            generateWallet(seed)
//            val keyPair = mnemonic.generateKeyPair()
//
//            Timber.d("PrivateKey = ${keyPair.privateKey.toHex()}")
//            binding.tvPrivateKey.text = keyPair.privateKey.toHex()
//
//            Timber.d("PublicKey = ${keyPair.publicKey.toHex()}")
//            binding.tvPublicKey.text = keyPair.publicKey.toHex()
        }


    }

    private fun Web3j.getClientVersion() = web3ClientVersion().sendAsync().get().web3ClientVersion

    private fun Web3j.getBlockNumber() = ethBlockNumber().sendAsync().get().blockNumber

    private fun getRandomInitialEntropy(numBytes: Int): ByteArray = SecureRandom().generateSeed(numBytes)

    private fun getMnemonic(numBytes: Int) = MnemonicUtils.generateMnemonic(getRandomInitialEntropy(numBytes))

    private fun String.generateSeed() = MnemonicUtils.generateSeed(this, null)

    private fun String.generateKeyPair() = Bip32ECKeyPair.generateKeyPair(this.generateSeed())

    private fun BigInteger.toHex(): String = toString(16)

    private fun ByteArray.toHex(): String {
        return BigInteger(this).toHex()
    }

    private fun generateWallet(seed: String): JSONObject {
        val processJson = JSONObject()
        try {
            val ecKeyPair = Keys.createEcKeyPair()
            val privateKeyInDec = ecKeyPair.privateKey
            val sPrivateKeyInHex = privateKeyInDec.toString(16)
            val aWallet = Wallet.createLight(seed, ecKeyPair)
            val sAddress = aWallet.address
            aWallet.crypto.cipher
            processJson.put("address", "0x$sAddress")
            processJson.put("privateKey", sPrivateKeyInHex)
        } catch (e: CipherException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return processJson
    }
}