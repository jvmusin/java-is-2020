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
        final int root;
        final int depth;
        final Predicate<UserInfo> predicate;
        final AtomicInteger totalGoodUsers;
        final Set<Integer> usedUsers;

        FindFollowersTask(int root, int depth, Predicate<UserInfo> predicate) {
            this.root = root;
            this.depth = depth;
            this.predicate = predicate;
            this.totalGoodUsers = new AtomicInteger(0);
            this.usedUsers = new ConcurrentSkipListSet<>();
        }

        void tryUpdateResult(UserInfo info) {
            if (predicate.test(info)) totalGoodUsers.incrementAndGet();
        }

        Collection<Integer> merge(Collection<Integer> a, Collection<Integer> b) {
            a.addAll(b);
            return a;
        }

        Future<Integer> run() {
            return run(singleton(root), depth);
        }

        CompletableFuture<Integer> run(Collection<Integer> roots, int depthLeft) {
            if (roots.isEmpty()) return completedFuture(totalGoodUsers.get());

            return roots.stream()
                    .map(r -> processUser(r, depthLeft > 0))
                    .reduce(completedFuture(new ArrayList<>()), (a, b) -> a.thenCombine(b, this::merge))
                    .thenCompose(newRoots -> run(newRoots, depthLeft - 1));
        }

        CompletableFuture<Collection<Integer>> processUser(int rootVertex, boolean takeNeighbours) {
            if (usedUsers.add(rootVertex)) {
                CompletableFuture<Void> f = network.getUserInfo(rootVertex).thenAccept(this::tryUpdateResult);
                if (takeNeighbours) {
                    CompletableFuture<Collection<Integer>> followers = network.getFollowers(rootVertex);
                    return f.thenCompose(Void -> followers);
                } else {
                    return f.thenApply(Void -> emptyList());
                }
            }
            return completedFuture(emptyList());
        }
    }
}
