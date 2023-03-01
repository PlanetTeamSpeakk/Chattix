package com.ptsmods.chattix.util;

import lombok.SneakyThrows;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.TextFilter;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// Currently unused as it is impossible at this time to use a text filter while retaining the original
// format of the message.
// The technical explanation of this is as follows:
// The game replaces any filtered out words and characters with hashtags on the client,
// it then reformats the message on the client, undecorated, with the chat type that the
// message uses. The chat type Chattix uses is a custom one (found in resources/data/chattix/chat_type)
// that only displays the message. The message is then decorated with the set format and this entire
// thing becomes the new message. The filter on the client, however, uses the original message and then
// reinserts that into the chat type. This results in a message that consists of only the message content
// with filtered out text and without anything else added in the format used. (So if someone were to say
// 'Hello World!' assuming 'World' is seen as a slur, the message that'd appear in chat would be 'Hello ****!'
// without a sender before it or anything of the sorts).

// There are two ways Mojang can fix this:
// 1. They allow for the use of custom parameters in chat type decorations. Currently, they only support
//    sender, target and message. This way, it wouldn't be an issue if the message is recreated on the client
//    as it will result in the exact same message as the server created. This would be the best solution
//    as it makes the entire procedure if decorating a message much neater.
// 2. They let the server apply the filtering and simply confirm they did it correctly on the client
//    rather than letting the client do the filtering and recreating the message.

// The only other possible way of doing a profanity filter is by actually replacing the content in the
// message with hashtags or asterisks ourselves, but then any player can hover over the question mark
// at the end of the message and see the message in all its glory with the original profane words anyway.
// This might be of some use as it's better than nothing (i.e. you have to put in effort yourself to see
// the slurs), but players can still be exposed to the profanity.
public class PurgoMalumTextFilter implements TextFilter {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.of(15, ChronoUnit.SECONDS))
            .build();

    @Override
    public void join() {}

    @Override
    public void leave() {}

    @NotNull
    @SneakyThrows
    @Override
    public CompletableFuture<FilteredText> processStreamMessage(String msg) {
        return client.sendAsync(HttpRequest.newBuilder()
                .uri(new URI("https://www.purgomalum.com/service/plain?text=" + URLEncoder.encode(msg, StandardCharsets.UTF_8)))
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    String result = resp.body();

                    // TODO actually do the filtering

                    return result.equals(msg) ? FilteredText.passThrough(msg) : FilteredText.fullyFiltered(msg);
                });
    }

    @NotNull
    @Override
    public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list) {
        return CompletableFuture.supplyAsync(() -> list.stream()
                .map(this::processStreamMessage)
                .map(CompletableFuture::join)
                .toList());
    }
}
