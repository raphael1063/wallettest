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

import org.json.JSONObject
import org.web3j.contracts.eip20.generated.ERC20
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.tx.Contract
import org.web3j.tx.exceptions.ContractCallException
import org.web3j.tx.gas.DefaultGasProvider
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException


class MainActivity : AppCompatActivity() {

    private val web3j: Web3j =
        Web3j.build(HttpService("https://fragrant-aged-wood.ropsten.quiknode.pro/bd3b90c5a36a4705dc374f9a7f4e00342926fedc/"))

    private var mnemonic = ""

    private var seed = ""

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        CoroutineScope(Dispatchers.IO).launch {
            try {
                Timber.d("Version = ${web3j.getClientVersion()}")
                Timber.d("BlockNumber = ${web3j.getBlockNumber()}")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnGenerateMnemonic.setOnClickListener {
            mnemonic = getMnemonic(32)
            Timber.d("Mnemonic = $mnemonic")
            binding.tvMnemonic.text = mnemonic

            seed = mnemonic.generateSeed().toHex()
            Timber.d("Seed = $seed")
            binding.tvSeed.text = seed
        }

        binding.btnCreateWallet.setOnClickListener {
            generateWallet(seed)
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

    private fun generateWallet(seed: String) {
       generateEthereum(seed)
    }

    private fun generateEthereum(seed: String): JSONObject {
        val processJson = JSONObject()
        try {
//            val ecKeyPair = Keys.createEcKeyPair()
            val ecKeyPair = mnemonic.generateKeyPair()
            val privateKeyInDec = ecKeyPair.privateKey
            val privateKeyInHex = privateKeyInDec.toString(16)
            val wallet = Wallet.createLight(seed, ecKeyPair)
            val walletAddress = "0x${wallet.address}"
            wallet.crypto.cipher
            processJson.put("address", "$walletAddress")
            processJson.put("privateKey", privateKeyInHex)
            Timber.d("Address = $walletAddress")
            Timber.d("PrivateKey = $privateKeyInHex")
            binding.tvPrivateKey.text = privateKeyInHex
            binding.tvPublicKey.text = ecKeyPair.publicKey.toString(16)
            binding.tvAddress.text = "0x$walletAddress"
           getBalance(walletAddress)


            val contractAddress = "0xa849eaae994fb86afa73382e9bd88c2b6b18dc71"
            getToken(ecKeyPair, contractAddress)
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

    private fun getBalance(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val balance = web3j.run {
                ethGetBalance(address, DefaultBlockParameterName.PENDING).sendAsync().get()
            }.balance
            Timber.d("Balance = $balance")
        }

    }

    private fun getToken(ecKeyPair: ECKeyPair, contractAddress: String) {
        val credentials = Credentials.create(ecKeyPair)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = ERC20.load(contractAddress, web3j, credentials, DefaultGasProvider())
                val tokenName = token.name().send()
                Timber.d("TokenName = $tokenName")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
}