import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.Date;


public class Main {
    public static void main(String[] args) {
        Worker worker = new Worker();

        worker.init("/opt/VPN/vpn.txt");
        worker.checkConsistency();

        try {
            TelegramBotsApi bot = new TelegramBotsApi(DefaultBotSession.class);
            sender botSender = new sender();
            botSender.setWorker(worker);
            bot.registerBot(botSender);

            while (true) {
                Thread.sleep(10000);
                if (!worker.inWork){
                    System.out.println("\t" + new Date() + "\nNew check:");
                    worker.checkConsistency();
                    System.out.println("Check completed!");
                    System.out.println("--------------------");
                }
            }

        } catch (TelegramApiException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}