public class User {
    private String Username;
    private String Password;
    private String NewOrOld;

    String getUsername(){
        return Username;
    }
    String getPassword(){
        return Password;
    }
    String getReturning(){
        return NewOrOld;
    }
    void setUsername(String username){
        Username = username;
    }
    void setPassword(String password){
        Password = password;
    }
    void setNewOrOld(String neworold){
        NewOrOld = neworold;
    }
}
