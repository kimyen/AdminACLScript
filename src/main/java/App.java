
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.sagebionetworks.client.SynapseAdminClient;
import org.sagebionetworks.client.SynapseAdminClientImpl;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.ResourceAccess;

import com.csvreader.CsvReader;

public class App {

    private static SynapseAdminClient adminSynapse;
    private final static String LOCAL_AUTH = "http://localhost:8080/services-repository-develop-SNAPSHOT/auth/v1";
    private final static String LOCAL_REPO = "http://localhost:8080/services-repository-develop-SNAPSHOT/repo/v1";
    private final static String LOCAL_FILE = "http://localhost:8080/services-repository-develop-SNAPSHOT/file/v1";
    private final static String STAGING_AUTH = "https://auth-staging.prod.sagebase.org/auth/v1";
    private final static String STAGING_REPO = "https://repo-staging.prod.sagebase.org/repo/v1";
    private final static String STAGING_FILE = "https://file-staging.prod.sagebase.org/file/v1";
	private static final Set<ACCESS_TYPE> ADMIN_ACCESS_SET = new HashSet<ACCESS_TYPE>(
			Arrays.asList(ACCESS_TYPE.CHANGE_PERMISSIONS, ACCESS_TYPE.CHANGE_SETTINGS,
					ACCESS_TYPE.CREATE, ACCESS_TYPE.READ, ACCESS_TYPE.UPDATE, ACCESS_TYPE.DELETE,
					ACCESS_TYPE.MODERATE));

    public static void main(String[] args) {
        if (args.length != 4) printUsage();
        String stack = args[0];
        String username = args[1];
        String apiKey = args[2];
        String filePath = args[3];

        adminSynapse = new SynapseAdminClientImpl();
        if (stack == null) {
            printUsage();
        }
        if (!stack.equals("prod")) {
            setEndPoint(adminSynapse, stack);
        }
        adminSynapse.setUserName(username);
        adminSynapse.setApiKey(apiKey);

        try {
            process(adminSynapse, filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private static void process(SynapseAdminClient adminSynapse, String filePath) throws IOException, SynapseException {
        CsvReader reader = new CsvReader(filePath);
        while (reader.readRecord()) {
            // update the acl
        	AccessControlList acl = adminSynapse.getACL("syn"+reader.get(0));
        	for (ResourceAccess ra : acl.getResourceAccess()) {
        		if (ra.getAccessType().contains(ACCESS_TYPE.CHANGE_PERMISSIONS)) {
        			ra.setAccessType(ADMIN_ACCESS_SET);
        		}
        	}
			adminSynapse.updateACL(acl);
        }
        reader.close();
    }

    private static void printUsage() {
        System.out.println("Usage: ");
        System.out.println("<prod/local/staging> <synapseUsername> <apiKey> <filePath>");
        System.exit(0);
    }

    private static void setEndPoint(SynapseAdminClient adminSynapse, String stack) {
        if (stack.equals("staging")) {
            adminSynapse.setAuthEndpoint(STAGING_AUTH);
            adminSynapse.setRepositoryEndpoint(STAGING_REPO);
            adminSynapse.setFileEndpoint(STAGING_FILE);
        } else if (stack.equals("local")){
            adminSynapse.setAuthEndpoint(LOCAL_AUTH);
            adminSynapse.setRepositoryEndpoint(LOCAL_REPO);
            adminSynapse.setFileEndpoint(LOCAL_FILE);
        } else {
            printUsage();
        }
    }
}
