package org.example.LLDQuestion.FileSystem;

/*
Functional Requirement
1) User should be able to create directory/file
2) user should be able to delete and remove file/directory
3) user should be able to add content in file
4) user should be able to read file
5) System should support different types of user(admin,guest,user)

 */




import java.util.*;
import java.nio.charset.StandardCharsets;

enum Role { ADMIN, NORMAL, GUEST }
enum Operation { CREATE, READ, WRITE, DELETE, GET_SIZE }

class User {
    String userId, name;
    Role role;

    User(String userId, String name, Role role) {
        this.userId = userId;
        this.name = name;
        this.role = role;
    }
}

class PermissionManager {
    Map<Role, Set<Operation>> permissions = new EnumMap<>(Role.class);

    PermissionManager() {
        permissions.put(Role.ADMIN, EnumSet.allOf(Operation.class));
        permissions.put(Role.NORMAL, EnumSet.allOf(Operation.class));
        permissions.put(Role.GUEST, EnumSet.of(Operation.READ, Operation.GET_SIZE));
    }

    void check(User user, Operation operation, FileSystemNode node) {
        if (user == null) throw new SecurityException("Login required");
        if (!permissions.getOrDefault(user.role, Collections.emptySet()).contains(operation))
            throw new SecurityException("Operation not allowed");

        if (user.role == Role.ADMIN) return;

        if (operation == Operation.CREATE && node.parentNode == null) return;

        if ((operation == Operation.READ || operation == Operation.GET_SIZE) && node.isPublic)
            return;

        if (user.role == Role.NORMAL && node.owner != null &&
                node.owner.userId.equals(user.userId)) return;

        throw new SecurityException("Access denied");
    }
}

abstract class FileSystemNode {
    String name;
    Directory parentNode;
    User owner;
    boolean isPublic = false;

    FileSystemNode(String name, Directory parentNode, User owner) {
        this.name = name;
        this.parentNode = parentNode;
        this.owner = owner;
    }

    String getPath() {
        if (parentNode == null) return "/";
        String parentPath = parentNode.getPath();
        return parentPath.equals("/") ? "/" + name : parentPath + "/" + name;
    }

    abstract long getSize();
}

class Directory extends FileSystemNode {
    Map<String, FileSystemNode> children = new HashMap<>();

    Directory(String name, Directory parentNode, User owner) {
        super(name, parentNode, owner);
    }

    void addNode(FileSystemNode node) {
        if (children.containsKey(node.name)) throw new RuntimeException("Already exists");
        children.put(node.name, node);
        node.parentNode = this;
    }

    void removeNode(String name) {
        FileSystemNode removed = children.remove(name);
        if (removed == null) throw new RuntimeException("Not found");
        removed.parentNode = null;
    }

    FileSystemNode getNode(String name) {
        return children.get(name);
    }

    @Override
    long getSize() {
        long size = 0;
        for (FileSystemNode node : children.values()) size += node.getSize();
        return size;
    }
}

class File extends FileSystemNode {
    StringBuilder content = new StringBuilder();

    File(String name, Directory parentNode, User owner) {
        super(name, parentNode, owner);
    }

    String read() {
        return content.toString();
    }

    void add(String text) {
        content.append(text);
    }

    @Override
    long getSize() {
        return content.toString().getBytes(StandardCharsets.UTF_8).length;
    }
}

class FileSystem {
    private final Directory root = new Directory("", null, null);
    private final PermissionManager permissionManager = new PermissionManager();


    private FileSystemNode getNodeByPath(String path) {
        if (path.equals("/")) return root;
        String[] parts = path.substring(1).split("/");
        FileSystemNode current = root;

        for (String part : parts) {
            if (!(current instanceof Directory)) throw new RuntimeException("Not a directory");
            current = ((Directory) current).getNode(part);
            if (current == null) throw new RuntimeException("Path not found: " + path);
        }
        return current;
    }

    private Directory getParentDirectory(String path) {
        if (path.equals("/")) throw new RuntimeException("Root has no parent");
        int index = path.lastIndexOf("/");
        String parentPath = index == 0 ? "/" : path.substring(0, index);
        FileSystemNode node = getNodeByPath(parentPath);
        if (!(node instanceof Directory)) throw new RuntimeException("Parent is not a directory");
        return (Directory) node;
    }

    private void checkSubtree(User user, Operation operation, FileSystemNode node) {
        permissionManager.check(user, operation, node);
        if (user.role == Role.ADMIN) return;
        if (node instanceof Directory) {
            for (FileSystemNode child : ((Directory) node).children.values())
                checkSubtree(user, operation, child);
        }
    }

    public void createDirectory(User user, String path) {
        Directory parent = getParentDirectory(path);
        permissionManager.check(user, Operation.CREATE, parent);
        String name = path.substring(path.lastIndexOf("/") + 1);
        parent.addNode(new Directory(name, parent, user));
    }

    public void createFile(User user, String path) {
        Directory parent = getParentDirectory(path);
        permissionManager.check(user, Operation.CREATE, parent);
        String name = path.substring(path.lastIndexOf("/") + 1);
        parent.addNode(new File(name, parent, user));
    }

    public void addContent(User user, String path, String content) {
        FileSystemNode node = getNodeByPath(path);
        permissionManager.check(user, Operation.WRITE, node);
        if (!(node instanceof File)) throw new RuntimeException("Not a file");
        ((File) node).add(content);
    }

    public String readFile(User user, String path) {
        FileSystemNode node = getNodeByPath(path);
        permissionManager.check(user, Operation.READ, node);
        if (!(node instanceof File)) throw new RuntimeException("Not a file");
        return ((File) node).read();
    }

    public void delete(User user, String path) {
        FileSystemNode node = getNodeByPath(path);
        if (node == root) throw new RuntimeException("Cannot delete root");
        checkSubtree(user, Operation.DELETE, node);
        node.parentNode.removeNode(node.name);
    }

    public long getSize(User user, String path) {
        FileSystemNode node = getNodeByPath(path);
        checkSubtree(user, Operation.GET_SIZE, node);
        return node.getSize();
    }

}

public class FileSystemDemo {
    public static void main(String[] args) {
        User admin = new User("1", "Admin", Role.ADMIN);
        User aman = new User("2", "Aman", Role.NORMAL);
        User raj = new User("3", "Raj", Role.NORMAL);
        User guest = new User("4", "Guest", Role.GUEST);
        FileSystem fs = new FileSystem();

        fs.createDirectory(aman, "/documents");
        fs.createDirectory(aman, "/documents/projects");
        fs.createFile(aman, "/documents/a.txt");
        fs.createFile(aman, "/documents/projects/b.txt");
        fs.addContent(aman, "/documents/a.txt", "Hello World");
        fs.addContent(aman, "/documents/projects/b.txt", "Java LLD");

        System.out.println(fs.readFile(aman, "/documents/a.txt"));
        System.out.println("File size: " + fs.getSize(aman, "/documents/a.txt"));
        System.out.println("Folder size: " + fs.getSize(aman, "/documents"));
        System.out.println("Admin read: " + fs.readFile(admin, "/documents/a.txt"));

        System.out.println("Guest read: " + fs.readFile(guest, "/documents/a.txt"));
        System.out.println("Raj read: " + fs.readFile(raj, "/documents/a.txt"));

        try {
            fs.delete(guest, "/documents/a.txt");
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
        }

        fs.delete(aman, "/documents/a.txt");
        fs.delete(admin, "/documents");
    }
}
