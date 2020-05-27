package impl.network;

import api.network.FollowersStats;
import api.network.SocialNetwork;
import api.network.UserInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class FollowersStatsImpl implements FollowersStats {

    private final SocialNetwork network;

    public FollowersStatsImpl(SocialNetwork network) {
        this.network = network;
    }

    @Override
    public Future<Integer> followersCountBy(int id, int depth, Predicate<UserInfo> predicate) {
        return new FindFollowersTask(id, depth, predicate).run();
    }

    private class FindFollowersTask {
        final int startUserId;
        final int depth;
        final Predicate<UserInfo> predicate;
        final AtomicInteger totalGoodUsers;
        final Set<Integer> usedUsers;

        FindFollowersTask(int startUserId, int depth, Predicate<UserInfo> predicate) {
            this.startUserId = startUserId;
            this.depth = depth;
            this.predicate = predicate;
            this.totalGoodUsers = new AtomicInteger();
            this.usedUsers = new ConcurrentSkipListSet<>();
        }

        void tryUpdateResult(UserInfo info) {
            if (predicate.test(info)) totalGoodUsers.incrementAndGet();
        }

        Collection<Integer> mergeCollections(Collection<Integer> a, Collection<Integer> b) {
            a.addAll(b);
            return a;
        }

        CompletableFuture<Collection<Integer>> mergeResults(
                CompletableFuture<Collection<Integer>> a,
                CompletableFuture<Collection<Integer>> b) {
            return a.thenCombine(b, this::mergeCollections);
        }

        Future<Integer> run() {
            return run(singleton(startUserId), depth);
        }

        CompletableFuture<Integer> run(Collection<Integer> users, int depthLeft) {
            if (users.isEmpty()) return completedFuture(totalGoodUsers.get());

            return users.stream()
                    .map(userId -> processUser(userId, depthLeft > 0))
                    .reduce(completedFuture(new ArrayList<>()), this::mergeResults)
                    .thenCompose(newUsers -> run(newUsers, depthLeft - 1));
        }

        CompletableFuture<Collection<Integer>> processUser(int userId, boolean takeNeighbours) {
            if (!usedUsers.add(userId)) return completedFuture(emptyList());

            CompletableFuture<Void> f = network.getUserInfo(userId).thenAccept(this::tryUpdateResult);
            CompletableFuture<Collection<Integer>> followers = takeNeighbours
                    ? network.getFollowers(userId)
                    : completedFuture(emptyList());
            return f.thenCompose(v -> followers);
        }
    }
}
