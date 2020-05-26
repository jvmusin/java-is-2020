package impl.network;

import api.network.FollowersStats;
import api.network.SocialNetwork;
import api.network.UserInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

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
            this.totalGoodUsers = new AtomicInteger();
            this.usedUsers = new ConcurrentSkipListSet<>();
        }

        void tryUpdateResult(UserInfo info) {
            if (predicate.test(info)) totalGoodUsers.incrementAndGet();
        }

        Future<Integer> run() {
            return ForkJoinPool.commonPool().submit(() -> {
                totalGoodUsers.set(0);
                List<Integer> verticesToVisit = new ArrayList<>();
                verticesToVisit.add(root);
                for (int d = 0; d <= depth; d++) {
                    boolean takeNeighbours = d < depth;
                    List<Integer> nextVertices = ForkJoinPool.commonPool().submit(() -> verticesToVisit.stream().parallel()
                            .map(v -> processUser(v, takeNeighbours))
                            .map(CompletableFuture::join)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList())
                    ).join();
                    verticesToVisit.clear();
                    verticesToVisit.addAll(nextVertices);
                }
                return totalGoodUsers.get();
            });
        }

        private CompletableFuture<Collection<Integer>> processUser(int rootVertex, boolean takeNeighbours) {
            if (usedUsers.add(rootVertex)) {
                CompletableFuture<Void> f = network.getUserInfo(rootVertex).thenAccept(FindFollowersTask.this::tryUpdateResult);
                if (takeNeighbours) {
                    CompletableFuture<Collection<Integer>> followers = network.getFollowers(rootVertex);
                    return f.thenCompose(Void -> followers);
                } else {
                    return f.thenApply(Void -> emptyList());
                }
            }
            return CompletableFuture.completedFuture(emptyList());
        }
    }
}
