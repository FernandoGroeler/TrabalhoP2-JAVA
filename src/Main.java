import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private int loop;
    private long startTime;
    private long stopTime;

    //-> Converter bytes
    private int byteSize(int bytes) {
        return (1024 * bytes);
    }

    //-> Preencher o arquivo
    private String fillFile(int count) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < count; i++) {
            stringBuilder.append("0");
        }

        return stringBuilder.toString();
    }

    //-> Preencher o arquivo com 4KB
    private String fillFile4kb() {
        return this.fillFile(byteSize(4));
    }

    //-> Criar o arquivo própriamente dito
    private void createFile(File file, String value) {
        try {
            if (file.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(value.getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-> Criar o arquivo
    private void createFile(String name, String value) {
        File file = new File(name);

        if (file.exists()) {
            file.delete();
        }

        createFile(file, value);
    }

    //-> Sobrescrever o arquivo
    private void overrideFile(String name, String value) {
        File file = new File(name);
        createFile(file, value);
    }

    //-> Ler o arquivo
    private void readFile(String name) {
        File file = new File(name);

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            while (bufferedInputStream.read() != -1){}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-> Tempo de processamento
    private long secondsElapsedTime(long startTime, long stopTime) {
        return ((stopTime - startTime)/1000);
    }

    //-> Gravar arquivo sequencial
    private void writeSequential() {
        startTime = System.currentTimeMillis();
        createFile("read.byt", fillFile(byteSize(200000))); //-> Arquivo de 200MB
        stopTime = System.currentTimeMillis();
        System.out.println("Gravação: " + secondsElapsedTime(startTime, stopTime) + " segundos.");
    }

    //-> Ler arquivo sequencial
    private void readSequential() {
        createFile("read.byt", fillFile(byteSize(200000))); //-> Arquivo de 200MB

        startTime = System.currentTimeMillis();
        readFile("read.byt");
        stopTime = System.currentTimeMillis();
        System.out.println("Leitura: " + secondsElapsedTime(startTime, stopTime) + " segundos.");
    }

    //-> Ler e gravar arquivo sequencial (minificar a chamada)
    private void readWriteSequential() {
        System.out.println("Taxa de leitura/gravação sequencial.......................................:\n");
        writeSequential();
        readSequential();
        System.out.println("---------------------------------------------------------------------------\n");
    }

    //-> Gravar arquivo em blocos
    private void writeBlocks() {
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 262144; i++) {
            overrideFile("writeblocks.byt", fillFile4kb());
        }
        stopTime = System.currentTimeMillis();
        System.out.println("Gravação: " + secondsElapsedTime(startTime, stopTime) + " segundos.");
    }

    //-> Ler arquivo em blocos
    private void readBlocks() {
        createFile("readblocks.byt", fillFile4kb());

        startTime = System.currentTimeMillis();
        for (int i = 0; i < 262144; i++) {
            readFile("readblocks.byt");
        }
        stopTime = System.currentTimeMillis();
        System.out.println("Leitura: " + secondsElapsedTime(startTime, stopTime) + " segundos.");
    }

    //-> Ler e gravar arquivo em blocos (minificar a chamada)
    private void readWriteBlocks() {
        System.out.println("Taxa de leitura/gravação de blocos 4k.....................................:\n");
        writeBlocks();
        readBlocks();
        System.out.println("---------------------------------------------------------------------------\n");
    }

    //-> Gravar arquivo em thread
    private void writeThread() {
        startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(64);

        for (loop = 0; loop < 64; loop++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < (262144 / 64); j++) {
                        overrideFile("writethread" + loop + ".byt", fillFile4kb());
                    }
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopTime = System.currentTimeMillis();
        System.out.println("Gravação: " + secondsElapsedTime(startTime, stopTime) + " segundos.");
    }

    //-> Ler arquivo em thread
    private void readThread() {
        createFile("readthread.byt", fillFile4kb());

        startTime = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(64);

        for (loop = 0; loop < 64; loop++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < (262144 / 64); j++) {
                        readFile("readthread.byt");
                    }
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopTime = System.currentTimeMillis();
        System.out.println("Leitura: " + secondsElapsedTime(startTime, stopTime) + " segundos.");
    }

    //-> Ler e gravar arquivo em thread (para minificar a chamada)
    private void readWriteThread() {
        System.out.println("Taxa de leitura/gravação em paralelo de blocos de 4KB (utilize 64 threads):\n");
        writeThread();
        readThread();
        System.out.println("---------------------------------------------------------------------------\n");
    }

    //-> Inicio
    public Main() {
        readWriteSequential();
        readWriteBlocks();
        readWriteThread();
    }

    public static void main(String[] args) {
        new Main();
        System.out.println("Pronto!");
    }
}
