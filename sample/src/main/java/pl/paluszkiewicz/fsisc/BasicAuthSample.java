package pl.paluszkiewicz.fsisc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;

import static org.slf4j.LoggerFactory.getLogger;

public class BasicAuthSample {

    private static final Logger LOG = getLogger(BasicAuthSample.class);
    private static final int HTTP_PORT = 32500;

    public static void main(String[] args) throws IOException {

        Path root = Path.of("secrets");

        FileSystemWatcher fsWatcher = FileSystemWatcher.defaultWatcher(root);
        RefreshableSecret<FileSecret> secret = RefreshableSecret.nullOnDelete();
        fsWatcher.watch(FileSecretPath.of("basicAuth.txt"), secret);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        FsStartConfig startConfig = new FsStartConfig(executorService);

        try {
            boolean result = fsWatcher.start(startConfig);
            LOG.debug("Watcher start result: {}", result);
        } catch (Exception e) {
            LOG.error("Error", e);
        }

        URI server = HttpProtectedServer.start(HTTP_PORT);
        var client = new HttpProtectedClient(server, secret);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> logResponse(client), 1, 1, SECONDS);
    }


    private static void logResponse(HttpProtectedClient client) {
        LOG.info("Response status from protected server: {}", client.get());
    }

    private static final class HttpProtectedClient {

        private final HttpClient client;
        private final Secret secret;
        private final URI uri;

        private HttpProtectedClient(URI uri, Secret secret) {
            this.secret = secret;
            this.client = HttpClient.newHttpClient();
            this.uri = uri;
        }

        // secret must be in format username<line separator>password, e.g. joe\nsecret
        private Optional<String> basicAuthHeader() {
            var lines = Optional.ofNullable(secret.secret())
                    .map(String::new)
                    .stream().flatMap(String::lines)
                    .collect(Collectors.toList());

            if (lines.size() != 2) {
                LOG.error("Expected two lines in secret, actual: {}", lines.size());
                return Optional.empty();
            }

            return Optional.of("Basic " + new Credentials(lines).encodeBase64());
        }

        private record Credentials(String user, String pwd) {

            private static final Encoder ENCODER = Base64.getEncoder();

            Credentials(List<String> list) {
                this(list.get(0), list.get(1));
            }

            String encodeBase64() {
                String formatted = "%s:%s".formatted(user, pwd);
                byte[] bytes = formatted.getBytes(UTF_8);
                return new String(ENCODER.encode(bytes));
            }
        }

        int get() {
            var builder = HttpRequest.newBuilder().GET().uri(this.uri);
            basicAuthHeader().ifPresentOrElse(
                    hV -> {
                        builder.header("Authorization", hV);
                        LOG.debug("Sending request with Authorization header: " + hV);
                    },
                    () -> LOG.debug("Sending request without Authorization header.")
            );
            var request = builder.build();
            try {
                return client.send(request, (BodyHandler<Integer>) responseInfo -> new BodySubscriber<>() {
                    @Override
                    public CompletionStage<Integer> getBody() {
                        return CompletableFuture.completedFuture(responseInfo.statusCode());
                    }

                    @Override
                    public void onSubscribe(Subscription subscription) {

                    }

                    @Override
                    public void onNext(List<ByteBuffer> item) {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }).body();
            } catch (IOException | InterruptedException e) {
                LOG.error("Could not send GET to the server. Error was: ", e);
                return -1;
            }
        }
    }

    private static final class HttpProtectedServer {

        static URI start(int port) throws IOException {
            var host = "localhost";
            var server = HttpServer.create(new InetSocketAddress(host, port), 0);
            var context = server.createContext("/");
            context.setAuthenticator(new FreshDatePasswordAuth());
            context.setHandler((exchange) -> exchange.sendResponseHeaders(200, 0));
            server.start();
            return URI.create("http://%s:%d".formatted(host, port));
        }
    }

    private static final class FreshDatePasswordAuth extends BasicAuthenticator {

        private FreshDatePasswordAuth() {
            super("realm", UTF_8);
        }

        public boolean checkCredentials(String username, String password) {
            LOG.debug("Credentials: {}, {}", username, password);
            return username.equals("user") && password.equals("secret");
        }
    }
}
