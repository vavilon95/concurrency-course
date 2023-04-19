package course.concurrency.exams.refactoring;

import course.concurrency.exams.refactoring.Others.MountTableManager;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MountTableRefresherService {

  private final MountTableManager manager;
  private Others.RouterStore routerStore = new Others.RouterStore();
  private long cacheUpdateTimeout;
  /**
   * All router admin clients cached. So no need to create the client again and again. Router admin
   * address(host:port) is used as key to cache RouterClient objects.
   */
  private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;
  /** Removes expired RouterClient from routerClientsCache. */
  private ScheduledExecutorService clientCacheCleanerScheduler;
  /** Refresh mount table cache of this router as well as all other routers. */
  private ExecutorService executor = Executors.newCachedThreadPool();

  public MountTableRefresherService(MountTableManager manager) {
    this.manager = manager;
  }

  public void serviceInit() {
    long routerClientMaxLiveTime = 15L;
    this.cacheUpdateTimeout = 100L;
    routerClientsCache = new Others.LoadingCache<String, Others.RouterClient>();
    routerStore.getCachedRecords().stream()
        .map(Others.RouterState::getAdminAddress)
        .forEach(addr -> routerClientsCache.add(addr, new Others.RouterClient()));

    initClientCacheCleaner(routerClientMaxLiveTime);
  }

  public void serviceStop() {
    executor.shutdownNow();
    //    clientCacheCleanerScheduler.shutdown();
    // remove and close all admin clients
    routerClientsCache.cleanUp();
  }

  private void initClientCacheCleaner(long routerClientMaxLiveTime) {
    ThreadFactory tf =
        r -> {
          Thread t = new Thread();
          t.setName("MountTableRefresh_ClientsCacheCleaner");
          t.setDaemon(true);
          return t;
        };

    clientCacheCleanerScheduler = Executors.newSingleThreadScheduledExecutor(tf);
    /*
     * When cleanUp() method is called, expired RouterClient will be removed and
     * closed.
     */
    clientCacheCleanerScheduler.scheduleWithFixedDelay(
        () -> routerClientsCache.cleanUp(),
        routerClientMaxLiveTime,
        routerClientMaxLiveTime,
        TimeUnit.MILLISECONDS);
  }

  public void refresh() {
    var features =
        routerStore.getCachedRecords().stream()
            .map(routerState -> getRefreshThread(routerState.getAdminAddress()))
            .map(
                routerState ->
                    CompletableFuture.supplyAsync(
                            () -> {
                              routerState.run();
                              return routerState;
                            },
                            executor)
                        .orTimeout(cacheUpdateTimeout, TimeUnit.MILLISECONDS)
                        .exceptionally(throwable -> routerState))
            .toArray(CompletableFuture[]::new);

    var results =
        CompletableFuture.allOf(features)
            .thenApply(
                r ->
                    Stream.of(features)
                        .map(feature -> (MountTableRefresherTask) feature.join())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
            .join();
    logResult(results);
  }

  private MountTableRefresherTask getRefreshThread(String adminAddress) {
    if (adminAddress == null || adminAddress.length() == 0) {
      // this router has not enabled router admin.
      return null;
    }
    if (isLocalAdmin(adminAddress)) {
      /*
       * Local router's cache update does not require RPC call, so no need for
       * RouterClient
       */
      return getLocalRefresher(adminAddress);
    }
    return new MountTableRefresherTask(manager, adminAddress);
  }

  protected MountTableRefresherTask getLocalRefresher(String adminAddress) {
    return new MountTableRefresherTask(manager, adminAddress);
  }

  private void removeFromCache(String adminAddress) {
    routerClientsCache.invalidate(adminAddress);
  }

  private boolean isLocalAdmin(String adminAddress) {
    return adminAddress.contains("local");
  }

  private void logResult(List<MountTableRefresherTask> refreshThreads) {
    int successCount = 0;
    int failureCount = 0;
    for (MountTableRefresherTask mountTableRefreshThread : refreshThreads) {
      if (mountTableRefreshThread.isSuccess()) {
        successCount++;
      } else {
        failureCount++;
        // remove RouterClient from cache so that new client is created
        removeFromCache(mountTableRefreshThread.getAdminAddress());
      }
    }
    log(
        String.format(
            "Mount table entries cache refresh successCount=%d,failureCount=%d",
            successCount, failureCount));
    if (failureCount > 0) {
      log("Not all router admins updated their cache");
    }
  }

  public void log(String message) {
    System.out.println(message);
  }

  public void setCacheUpdateTimeout(long cacheUpdateTimeout) {
    this.cacheUpdateTimeout = cacheUpdateTimeout;
  }

  public void setRouterClientsCache(Others.LoadingCache cache) {
    this.routerClientsCache = cache;
  }

  public void setRouterStore(Others.RouterStore routerStore) {
    this.routerStore = routerStore;
  }
}
