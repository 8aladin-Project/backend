package potato.backend.global.config;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Infisical CLI를 사용한 인증 및 명령 실행 유틸리티
 * 
 * ProcessBuilder를 사용하여 Infisical CLI 명령을 실행하고
 * Universal Auth를 통해 토큰을 획득합니다.
 * 
 * 사용 방법:
 * 1. 환경 변수 설정:
 *    - INFISICAL_CLIENT_ID
 *    - INFISICAL_CLIENT_SECRET
 * 
 * 2. 토큰 획득 및 명령 실행:
 *    String token = InfisicalAuth.loginAndGetToken(clientId, clientSecret);
 *    InfisicalAuth.runWithToken(token, new String[]{"secrets", "get", "MY_SECRET"});
 */
@Slf4j
public class InfisicalAuth {

    /**
     * Infisical CLI를 사용하여 로그인하고 인증 토큰을 획득합니다.
     *
     * @param clientId Universal Auth의 Client ID
     * @param clientSecret Universal Auth의 Client Secret
     * @return 인증 토큰
     * @throws IOException 프로세스 실행 중 I/O 오류 발생 시
     * @throws InterruptedException 프로세스 대기 중 인터럽트 발생 시
     * @throws RuntimeException 로그인 실패 시
     */
    public static String loginAndGetToken(String clientId, String clientSecret) 
            throws IOException, InterruptedException {
        log.info("Attempting to login to Infisical using Universal Auth...");
        
        ProcessBuilder pb = new ProcessBuilder(
            "infisical", "login",
            "--method=universal-auth",
            "--client-id=" + clientId,
            "--client-secret=" + clientSecret,
            "--plain", "--silent"
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String token = br.lines().collect(Collectors.joining("\n")).trim();
            int exit = p.waitFor();
            
            if (exit != 0) {
                log.error("Infisical login failed with exit code: {}", exit);
                log.error("Token output: {}", token);
                throw new RuntimeException("Infisical login failed, exit code " + exit);
            }
            
            log.info("✓ Successfully obtained Infisical authentication token");
            return token;
        }
    }

    /**
     * 인증 토큰을 사용하여 Infisical CLI 명령을 실행합니다.
     *
     * @param token 인증 토큰
     * @param command 실행할 명령 배열 (예: ["secrets", "get", "MY_SECRET"])
     * @throws IOException 프로세스 실행 중 I/O 오류 발생 시
     * @throws InterruptedException 프로세스 대기 중 인터럽트 발생 시
     * @throws RuntimeException 명령 실행 실패 시
     */
    public static void runWithToken(String token, String[] command) 
            throws IOException, InterruptedException {
        log.debug("Executing Infisical command: {}", Arrays.toString(command));
        
        List<String> cmd = new ArrayList<>();
        cmd.add("infisical");
        cmd.addAll(Arrays.asList(command));
        cmd.add("--token=" + token);
        
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();  // 표준출력/에러 공유
        Process p = pb.start();
        int exit = p.waitFor();
        
        if (exit != 0) {
            log.error("Infisical command failed with exit code: {}", exit);
            throw new RuntimeException("Infisical command failed with exit " + exit);
        }
        
        log.debug("✓ Infisical command executed successfully");
    }

    /**
     * 인증 토큰을 사용하여 Infisical CLI 명령을 실행하고 출력을 반환합니다.
     *
     * @param token 인증 토큰
     * @param command 실행할 명령 배열
     * @return 명령 실행 결과 출력
     * @throws IOException 프로세스 실행 중 I/O 오류 발생 시
     * @throws InterruptedException 프로세스 대기 중 인터럽트 발생 시
     * @throws RuntimeException 명령 실행 실패 시
     */
    public static String runWithTokenAndCapture(String token, String[] command) 
            throws IOException, InterruptedException {
        log.debug("Executing Infisical command with output capture: {}", Arrays.toString(command));
        
        List<String> cmd = new ArrayList<>();
        cmd.add("infisical");
        cmd.addAll(Arrays.asList(command));
        cmd.add("--token=" + token);
        
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String output = br.lines().collect(Collectors.joining("\n")).trim();
            int exit = p.waitFor();
            
            if (exit != 0) {
                log.error("Infisical command failed with exit code: {}", exit);
                log.error("Output: {}", output);
                throw new RuntimeException("Infisical command failed with exit " + exit);
            }
            
            log.debug("✓ Infisical command executed successfully");
            return output;
        }
    }

    /**
     * 사용 예시를 보여주는 메인 메서드
     */
    public static void main(String[] args) throws Exception {
        String clientId = System.getenv("INFISICAL_CLIENT_ID");
        String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
        
        if (clientId == null || clientSecret == null) {
            System.err.println("❌ INFISICAL_CLIENT_ID and INFISICAL_CLIENT_SECRET must be set");
            System.exit(1);
        }
        
        // 1. 토큰 획득
        String token = loginAndGetToken(clientId, clientSecret);
        System.out.println("Obtained token: " + token);

        // 2. 예시: secrets get 명령 실행
        runWithToken(token, new String[]{"secrets", "get", "MY_SECRET", "--plain", "--silent"});
        
        // 3. 출력 캡처 예시
        String secretValue = runWithTokenAndCapture(token, 
            new String[]{"secrets", "get", "MY_SECRET", "--plain", "--silent"});
        System.out.println("Secret value: " + secretValue);
    }
}
