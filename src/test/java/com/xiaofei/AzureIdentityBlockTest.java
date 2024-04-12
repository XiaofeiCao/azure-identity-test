package com.xiaofei;


import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Test for reproducing thread stuck when using ClientSecretCredential.
 */
public class AzureIdentityBlockTest {
    @Test
    public void clientSecretCredentialGetTokenTest() {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(System.getenv("AZURE_CLIENT_ID"))
                .clientSecret(System.getenv("AZURE_CLIENT_SECRET"))
                .tenantId(System.getenv("AZURE_TENANT_ID"))
                // make it run on another thread pool other than the default will resolve the hang
//                .executorService(Executors.newCachedThreadPool())
                .build();
        // make it greater than the actual cpu core count will result in hang
        int parallelism = 100;
        IntStream.range(1, parallelism).parallel().forEach(iii -> {
            credential.getToken(new TokenRequestContext().addScopes("https://management.core.windows.net//.default")).block();
            // use getTokenSync will also resolve the hang
//            credential.getTokenSync(new TokenRequestContext().addScopes("https://management.core.windows.net//.default"));

            System.out.println("fetched for " + iii);
        });
    }
}
