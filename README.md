# Application Security 
Our **AppSec** is a secure web application combining steganography using Koch and Zhao's algorithm. Designed as a Progressive Web Application (PWA), implemented with Jakarta EE.

---

## 🚀 Technology Stack
- **Frontend**: Progressive Web Application (PWA)
- **Middleware**: Jakarta EE


## Here are the steps to deploy and access our project on WildFly:

1. Add Management User:

./add-user.sh

Follow prompts to create a management user.


2. Start WildFly:

./standalone.sh


3. Build Project:

mvn clean package


4. Deploy via CLI:
   Open a new terminal and connect to WildFly CLI:

./jboss-cli.sh --connect
deploy /path/to/your_project.war


5. Access Application:
   Open your browser and visit:

http://<server-ip>:8080/your_project

## OAuth
The web application will integrate with an OAuth provider to authenticate users.
When a user tries to access the app, they will be redirected to the OAuth provider's login page.
After successful authentication, the OAuth provider will issue an authorization code, which the web application will exchange for an access token.
OAuth will allow users to grant access to certain parts of the web application without revealing their credentials.
OAuth will be combined with role-based access control, ensuring that after a user logs in, their roles and permissions are fetched from the IAM system, and the access granted to certain app functionalities will depend on their role (e.g., admin, user, guest).

## JWT
After the user successfully authenticates via OAuth, the server will issue a JWT token. This token will be used to authenticate and authorize the user in subsequent requests.
The JWT token will contain important claims such as:
The user’s identity (user ID or username).
The user’s roles and permissions (access rights).
The expiration time of the token to ensure security.
The client (frontend) will store this token, typically in localStorage or cookies, and include it in the Authorization header for every API request made to the server.
When the client makes a request, the server will extract the JWT from the Authorization header.
The server will then verify the token’s integrity and ensure it hasn’t been tampered with. If the token is valid, the server will extract the claims and check whether the user has the required permissions for the requested resource.
JWTs will have a short expiration time (e.g., 1 hour). After this time, the user will be required to re-authenticate.
A refresh token mechanism will be implemented, allowing the user to obtain a new JWT without requiring them to log in again (as long as the refresh token is valid).

## Screenshots
### Steganography Page
![Welcome Page Screenshot](/src/main/webapp/assets/images/steg.png)

### Log In 
![Log In Screenshot](/src/main/webapp/assets/images/login.png)  

### Sign Up
![Signup Screenshot](/src/main/webapp/assets/images/signup.png) 


### Creating a .war file
![warfile Screenshot](/src/main/webapp/assets/images/warfile.png)

### Local Testing
![Test Screenshot](/src/main/webapp/assets/images/test.png) 