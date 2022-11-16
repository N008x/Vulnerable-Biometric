package com.bimatric.app;

import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "KeyAlias";
    Executor ex;
    BiometricPrompt bp;
    BiometricPrompt.PromptInfo pi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button f = (Button) findViewById(R.id.fingerprint);
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ex = ContextCompat.getMainExecutor(getApplicationContext());

                bp = new BiometricPrompt(MainActivity.this, ex, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        Toast.makeText(MainActivity.this,"Success",Toast.LENGTH_LONG).show();
                        setContentView(R.layout.fingerprint_normal);
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);

                        Toast.makeText(MainActivity.this,errString,Toast.LENGTH_LONG).show();
                        MainActivity.this.finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();

                        Toast.makeText(MainActivity.this,"FAILED",Toast.LENGTH_LONG).show();
                    }
                });


                pi = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Touch id required")
                        .setDescription("Touch the touch id sensor")
                        .setNegativeButtonText("Exit")
                        .build();

                bp.authenticate(pi);
            }
        });

        Button fwe = (Button) findViewById(R.id.fingerprint_exception);
        fwe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyGenerator generator = null;
                try {
                    generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                }

                try {
                    generator.init(new KeyGenParameterSpec.Builder (KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .setUserAuthenticationRequired(true)
                            .build()
                    );
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }

                generator.generateKey();

                ex = ContextCompat.getMainExecutor(getApplicationContext());

                bp = new BiometricPrompt(MainActivity.this, ex, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result ) {
                        Toast.makeText(MainActivity.this,"Success",Toast.LENGTH_LONG).show();
                        setContentView(R.layout.fingerprint_exception);
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);

                        Toast.makeText(MainActivity.this,errString,Toast.LENGTH_LONG).show();
                        MainActivity.this.finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();

                        Toast.makeText(MainActivity.this,"FAILED",Toast.LENGTH_LONG).show();
                    }

                });

                pi = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Touch id required")
                        .setDescription("Touch the touch id sensor")
                        .setNegativeButtonText("Exit")
                        .build();
                try {
                    BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(getEncryptCipher(createKey()));
                    bp.authenticate(pi, cryptoObject);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private SecretKey createKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        String alg = KeyProperties.KEY_ALGORITHM_AES;
        String pr = "AndroidKeyStore";
        KeyGenerator keyGenerator = KeyGenerator.getInstance(alg, pr);
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder("MY_KEY", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .build();

        keyGenerator.init(keyGenParameterSpec);
        return keyGenerator.generateKey();
    }

    private Cipher getEncryptCipher(Key key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeyException {
        String algorithm = KeyProperties.KEY_ALGORITHM_AES;
        String blockMode = KeyProperties.BLOCK_MODE_CBC;
        String padding = KeyProperties.ENCRYPTION_PADDING_PKCS7;
        Cipher c = Cipher.getInstance(algorithm+"/"+blockMode+"/"+padding);
        c.init(Cipher.ENCRYPT_MODE, key);
        return c;
    }
}