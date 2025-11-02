# Java's Deliberate Evolution: A Study of Removed Features and Rejected Proposals

**Author:** Java Language Historian  
**Date:** 2024  
**Version:** 1.0

---

## Table of Contents

1. [Part 1: Removed Java Language Features](#part-1-removed-java-language-features)
2. [Part 2: Rejected or Withdrawn JDK Enhancement Proposals](#part-2-rejected-or-withdrawn-jdk-enhancement-proposals)
3. [Part 3: Deprecated Features to be Removed in Future Versions](#part-3-deprecated-features-to-be-removed-in-future-versions)

---

## Part 1: Removed Java Language Features

### 1.1 Object.finalize()

**Feature Name:** Object Finalization (finalize() method)

**Rationale for Removal:**
- **Unpredictable Timing:** Finalizers run at unpredictable times, making resource cleanup unreliable
- **Performance Impact:** Finalizers significantly slow down garbage collection and object allocation
- **Security Vulnerabilities:** Finalizer attacks could resurrect objects that should be destroyed
- **Complexity:** Difficult to implement correctly; prone to deadlocks and resource leaks
- **Better Alternatives:** try-with-resources and Cleaner API provide superior resource management

**Decommissioning Timeline:**
- **Java 9 (JEP 421):** Deprecated for removal
- **Java 18:** Finalization disabled by default (can be re-enabled)
- **Future Release:** Complete removal planned

**Code Examples:**

```java
// OLD: Flawed finalize() usage
public class DatabaseConnection {
    private Connection conn;
    
    public DatabaseConnection(String url) throws SQLException {
        this.conn = DriverManager.getConnection(url);
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close(); // Unreliable - may never execute
            }
        } finally {
            super.finalize();
        }
    }
}

// MODERN: Using try-with-resources
public class DatabaseConnection implements AutoCloseable {
    private Connection conn;
    
    public DatabaseConnection(String url) throws SQLException {
        this.conn = DriverManager.getConnection(url);
    }
    
    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    // Usage
    public static void main(String[] args) {
        try (DatabaseConnection db = new DatabaseConnection("jdbc:...")) {
            // Use connection - guaranteed cleanup
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// MODERN: Using Cleaner API for non-deterministic cleanup
public class ResourceHolder {
    private static final Cleaner cleaner = Cleaner.create();
    
    static class State implements Runnable {
        private NativeResource resource;
        
        State(NativeResource resource) {
            this.resource = resource;
        }
        
        @Override
        public void run() {
            if (resource != null) {
                resource.release();
            }
        }
    }
    
    private final State state;
    private final Cleaner.Cleanable cleanable;
    
    public ResourceHolder(NativeResource resource) {
        this.state = new State(resource);
        this.cleanable = cleaner.register(this, state);
    }
}
```

**Impact Analysis:**

**Pros of Removal:**
- Eliminates a major source of bugs and unpredictable behavior
- Improves GC performance significantly
- Closes security vulnerabilities
- Forces developers to use better resource management patterns

**Cons of Removal:**
- Legacy code requires refactoring
- Breaking change for applications relying on finalization
- Learning curve for developers unfamiliar with AutoCloseable and Cleaner

---

### 1.2 Java Applets

**Feature Name:** Java Applets (java.applet package)

**Rationale for Removal:**
- **Security Concerns:** Applets posed significant security risks in web browsers
- **Browser Support Ended:** Major browsers removed Java plugin support
- **Obsolescence:** Modern web technologies (JavaScript, WebAssembly) replaced applets
- **Maintenance Burden:** Keeping applet infrastructure secure was costly
- **User Experience:** Required plugin installation, slow startup times

**Decommissioning Timeline:**
- **Java 9 (JEP 289):** Deprecated the Applet API
- **Java 11:** Removed Java Web Start and Applet API from JDK
- **Java 17:** Complete removal of java.applet package

**Code Examples:**

```java
// OLD: Java Applet (no longer supported)
import java.applet.Applet;
import java.awt.Graphics;

public class HelloApplet extends Applet {
    @Override
    public void paint(Graphics g) {
        g.drawString("Hello, World!", 50, 25);
    }
}

// HTML embedding (obsolete)
// <applet code="HelloApplet.class" width="300" height="100"></applet>

// MODERN: JavaFX Application
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloJavaFX extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("Hello, World!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 300, 100);
        
        primaryStage.setTitle("Hello JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

// MODERN: Web-based alternative using Spring Boot + REST API
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class HelloWebApp {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
    
    public static void main(String[] args) {
        SpringApplication.run(HelloWebApp.class, args);
    }
}
```

**Impact Analysis:**

**Pros of Removal:**
- Eliminated major security attack vector
- Reduced JDK maintenance burden
- Aligned Java with modern web development practices
- Improved browser security and performance

**Cons of Removal:**
- Broke existing enterprise applications using applets
- Required costly migration projects
- Lost Java's presence in browser-based applications
- Some educational institutions had to update curricula

---

### 1.3 Thread.stop(), Thread.suspend(), Thread.resume()

**Feature Name:** Unsafe Thread Control Methods

**Rationale for Removal:**
- **Thread.stop():** Inherently unsafe - unlocks all monitors, causing inconsistent object state
- **Thread.suspend():** Prone to deadlocks - holds locks while suspended
- **Thread.resume():** Only useful with suspend(), equally problematic
- **Data Corruption:** Could leave objects in corrupted, partially-updated states
- **No Cleanup:** Prevented proper resource cleanup and exception handling

**Decommissioning Timeline:**
- **Java 1.2 (1998):** Deprecated these methods
- **Java 11:** Thread.destroy() and Thread.stop(Throwable) removed
- **Still Present:** Methods remain but throw UnsupportedOperationException

**Code Examples:**

```java
// OLD: Dangerous thread control (deprecated)
public class UnsafeThreadControl {
    private Thread workerThread;
    
    public void startWork() {
        workerThread = new Thread(() -> {
            while (true) {
                // Do work
                processData();
            }
        });
        workerThread.start();
    }
    
    public void stopWork() {
        workerThread.stop(); // DANGEROUS! Leaves objects inconsistent
    }
    
    public void pauseWork() {
        workerThread.suspend(); // DANGEROUS! Can cause deadlocks
    }
    
    public void resumeWork() {
        workerThread.resume(); // Only works with suspend()
    }
}

// MODERN: Safe thread control using interruption
public class SafeThreadControl {
    private Thread workerThread;
    private volatile boolean running = false;
    
    public void startWork() {
        running = true;
        workerThread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    processData();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            cleanup(); // Proper cleanup guaranteed
        });
        workerThread.start();
    }
    
    public void stopWork() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }
    
    private void processData() throws InterruptedException {
        // Check interruption status regularly
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        // Do work
    }
    
    private void cleanup() {
        // Release resources safely
    }
}

// MODERN: Using ExecutorService for better control
import java.util.concurrent.*;

public class ModernThreadControl {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> currentTask;
    
    public void startWork() {
        currentTask = executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    processData();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    public void stopWork() {
        if (currentTask != null) {
            currentTask.cancel(true); // Interrupts the thread
        }
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
```

**Impact Analysis:**

**Pros of Removal:**
- Prevents data corruption and inconsistent object states
- Eliminates deadlock-prone patterns
- Encourages proper thread lifecycle management
- Forces developers to handle interruption correctly

**Cons of Removal:**
- No direct replacement for forceful thread termination
- Requires more complex interrupt-aware code
- Legacy code needs significant refactoring
- Some use cases (like killing runaway threads) become harder

---

### 1.4 Security Manager

**Feature Name:** Security Manager (java.lang.SecurityManager)

**Rationale for Removal:**
- **Rarely Used:** Very few applications actually used Security Manager
- **Performance Overhead:** Imposed runtime checks on all security-sensitive operations
- **Complexity:** Difficult to configure correctly; steep learning curve
- **Maintenance Burden:** Costly to maintain for limited benefit
- **Better Alternatives:** OS-level security, containers, and module system provide better isolation

**Decommissioning Timeline:**
- **Java 17 (JEP 411):** Deprecated for removal
- **Java 18+:** Warns when Security Manager is enabled
- **Future Release:** Complete removal planned

**Code Examples:**

```java
// OLD: Using Security Manager
public class SecurityManagerExample {
    public static void main(String[] args) {
        // Install security manager
        System.setSecurityManager(new SecurityManager());
        
        try {
            // This will be checked by security manager
            System.setProperty("user.home", "/tmp");
        } catch (SecurityException e) {
            System.out.println("Security violation: " + e.getMessage());
        }
    }
}

// OLD: Custom Security Manager
public class CustomSecurityManager extends SecurityManager {
    @Override
    public void checkPermission(Permission perm) {
        if (perm instanceof FilePermission) {
            String actions = perm.getActions();
            if (actions.contains("write")) {
                throw new SecurityException("Write operations not allowed");
            }
        }
        super.checkPermission(perm);
    }
    
    @Override
    public void checkExit(int status) {
        throw new SecurityException("System.exit() not allowed");
    }
}

// MODERN: Using Java Module System for encapsulation
module com.example.secure {
    exports com.example.api; // Only export public API
    // Internal packages are hidden
    
    requires java.base;
    requires transitive java.sql; // Transitive dependency
}

// MODERN: Using OS-level security (Docker example)
// Dockerfile
/*
FROM openjdk:17-slim
RUN useradd -m -u 1000 appuser
USER appuser
COPY target/app.jar /app/app.jar
WORKDIR /app
CMD ["java", "-jar", "app.jar"]
*/

// MODERN: Using SecurityManager alternatives
import java.nio.file.*;
import java.io.IOException;

public class ModernSecurityApproach {
    private static final Path ALLOWED_DIR = Paths.get("/app/data");
    
    public void writeFile(String filename, String content) throws IOException {
        Path filePath = ALLOWED_DIR.resolve(filename).normalize();
        
        // Explicit validation instead of SecurityManager
        if (!filePath.startsWith(ALLOWED_DIR)) {
            throw new SecurityException("Access denied: path outside allowed directory");
        }
        
        Files.writeString(filePath, content);
    }
    
    // Prevent System.exit() by design
    public void shutdown() {
        // Graceful shutdown instead of System.exit()
        cleanup();
        // Let the application terminate naturally
    }
    
    private void cleanup() {
        // Release resources
    }
}
```

**Impact Analysis:**

**Pros of Removal:**
- Removes performance overhead from security checks
- Simplifies JDK codebase significantly
- Encourages modern security practices (containers, modules)
- Reduces maintenance burden

**Cons of Removal:**
- Breaks applications that rely on Security Manager
- No direct replacement for fine-grained permission control
- Some sandboxing use cases become more difficult
- Educational tools using Security Manager need alternatives

---

### 1.5 Primitive Wrapper Constructors

**Feature Name:** Primitive Wrapper Constructors (new Integer(), new Long(), etc.)

**Rationale for Removal:**
- **Memory Waste:** Created unnecessary objects instead of using cached instances
- **Performance:** Slower than valueOf() which uses caching
- **Confusion:** Developers didn't understand difference between new Integer(5) and Integer.valueOf(5)
- **Identity vs Equality:** new Integer(5) == new Integer(5) is false, causing bugs
- **Better Alternative:** valueOf() methods provide caching for common values

**Decommissioning Timeline:**
- **Java 9 (JEP 277):** Deprecated wrapper constructors
- **Future Release:** Removal planned (not yet removed)

**Code Examples:**

```java
// OLD: Using constructors (deprecated)
public class WrapperConstructors {
    public void oldWay() {
        Integer i1 = new Integer(100);        // Deprecated
        Integer i2 = new Integer("200");      // Deprecated
        Long l = new Long(300L);              // Deprecated
        Double d = new Double(4.5);           // Deprecated
        Boolean b = new Boolean(true);        // Deprecated
        
        // Identity problem
        Integer x = new Integer(5);
        Integer y = new Integer(5);
        System.out.println(x == y);           // false - different objects!
        System.out.println(x.equals(y));      // true - same value
    }
}

// MODERN: Using valueOf() and autoboxing
public class ModernWrappers {
    public void modernWay() {
        Integer i1 = Integer.valueOf(100);    // Uses cache for -128 to 127
        Integer i2 = Integer.valueOf("200");  // Parses string
        Long l = Long.valueOf(300L);          // Explicit valueOf
        Double d = Double.valueOf(4.5);       // No caching for Double
        Boolean b = Boolean.valueOf(true);    // Uses cached TRUE/FALSE
        
        // Better identity behavior with caching
        Integer x = Integer.valueOf(5);
        Integer y = Integer.valueOf(5);
        System.out.println(x == y);           // true - same cached object!
        
        // Autoboxing (preferred for literals)
        Integer i3 = 100;                     // Implicitly calls valueOf()
        Long l2 = 300L;
        Boolean b2 = true;
    }
    
    public void demonstrateCaching() {
        // Integer caches -128 to 127
        Integer a = 127;
        Integer b = 127;
        System.out.println(a == b);           // true - cached
        
        Integer c = 128;
        Integer d = 128;
        System.out.println(c == d);           // false - not cached
        System.out.println(c.equals(d));      // true - same value
    }
    
    public void parseStrings() {
        // Parsing without creating wrapper objects
        int primitiveInt = Integer.parseInt("42");
        long primitiveLong = Long.parseLong("1000");
        double primitiveDouble = Double.parseDouble("3.14");
        
        // When you need wrapper objects
        Integer wrapperInt = Integer.valueOf("42");
        Long wrapperLong = Long.valueOf("1000");
    }
}
```

**Impact Analysis:**

**Pros of Removal:**
- Reduces memory consumption through caching
- Improves performance by reusing objects
- Eliminates confusion about identity vs equality
- Encourages best practices

**Cons of Removal:**
- Requires code updates in legacy applications
- Developers must learn valueOf() pattern
- Some reflection-based code may break

---

### 1.6 RMI Activation

**Feature Name:** RMI Activation System (java.rmi.activation package)

**Rationale for Removal:**
- **Rarely Used:** Very few applications used RMI Activation
- **Complexity:** Overly complex for the problems it solved
- **Obsolescence:** Modern distributed systems use REST, gRPC, or messaging
- **Security Concerns:** Additional attack surface with limited benefit
- **Maintenance Burden:** Costly to maintain for minimal usage

**Decommissioning Timeline:**
- **Java 15 (JEP 385):** Deprecated RMI Activation for removal
- **Java 17:** Removed RMI Activation

**Code Examples:**

```java
// OLD: RMI Activation (removed)
import java.rmi.activation.*;
import java.rmi.*;

public class ActivatableService extends Activatable implements RemoteService {
    public ActivatableService(ActivationID id, MarshalledObject data) 
            throws RemoteException {
        super(id, 0);
    }
    
    @Override
    public String performService() throws RemoteException {
        return "Service result";
    }
    
    public static void main(String[] args) throws Exception {
        // Setup activation group
        Properties props = new Properties();
        ActivationGroupDesc groupDesc = 
            new ActivationGroupDesc(props, null);
        ActivationGroupID groupID = 
            ActivationGroup.getSystem().registerGroup(groupDesc);
        
        // Register activatable object
        ActivationDesc desc = new ActivationDesc(
            groupID, 
            "ActivatableService",
            null, 
            null
        );
        RemoteService stub = 
            (RemoteService) Activatable.register(desc);
    }
}

// MODERN: Standard RMI (without activation)
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;

public interface RemoteService extends Remote {
    String performService() throws RemoteException;
}

public class StandardRMIService extends UnicastRemoteObject 
        implements RemoteService {
    
    public StandardRMIService() throws RemoteException {
        super();
    }
    
    @Override
    public String performService() throws RemoteException {
        return "Service result";
    }
    
    // Server
    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegis