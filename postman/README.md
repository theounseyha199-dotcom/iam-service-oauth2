# Test with Postman Desktop

1. Import `IAM-Assignment.postman_collection.json`.
2. Import `IAM-Assignment.postman_environment.json` and select it. Set
   `oidc_client_secret` to the `IAM_OIDC_CLIENT_SECRET` you chose at startup and
   `registration_client_secret` to your `IAM_DCR_CLIENT_SECRET`. The portable
   export intentionally has no passwords or tokens. Any author's `*.private.*`
   environment is local only and is not required.
3. Click the **collection name**, open **Authorization**, and click **Get New Access
   Token**. Keep **Authorize using browser** unchecked. Sign in with your IAM account
   in the built-in window, approve consent, and click **Use Token**.
4. Open **01 - Sign in and check account → 01 - Check my login** and click **Send**.
   Expect 200. Other protected requests inherit the collection's selected OAuth token;
   no manual access-token copying is required. Use an administrator for admin routes.

The callback is `https://oauth.pstmn.io/v1/callback`, using authorization code with
SHA-256 PKCE and Basic client authentication. User login and consent remain interactive.
A username and password cannot be posted directly to the token endpoint.

Fixture UUIDs and password variables must be filled before mutation requests.
Run cleanup requests only against records you created for testing.
The portable environment contains no secrets. Never export selected OAuth tokens
or private environment values into version control.
