package net.minecrell.quartz.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.DigestInputStream
import java.security.MessageDigest

class DownloadServerTask extends DefaultTask {

    @Input
    String url

    @OutputFile
    File output

    @TaskAction
    void download() throws IOException {
        URL url = new URL(this.url)
        URLConnection con = url.openConnection()
        MessageDigest md5 = MessageDigest.getInstance('MD5')

        Path output = this.output.toPath()

        ReadableByteChannel source = Channels.newChannel(new DigestInputStream(con.getInputStream(), md5))
        source.withCloseable {
            FileChannel out = FileChannel.open(output, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
            out.withCloseable {
                out.transferFrom(source, 0, Long.MAX_VALUE)
            }
        }

        def expected = getEtag(con)
        if (!expected.isEmpty()) {
            def hash = md5.digest().encodeHex().toString()
            if (hash.equals(expected)) {
                logger.debug("Successfully downloaded Minecraft server")
            } else {
                Files.delete output
                throw new IllegalStateException("Checksum mismatch while downloading")
            }
        }
    }

    private static String getEtag(URLConnection con) {
        def hash = con.getHeaderField("ETag")
        if (hash == null || hash.isEmpty()) {
            return ""
        }

        if (hash.startsWith("\"") && hash.endsWith("\"")) {
            hash = hash.substring(1, hash.length() - 1)
        }

        hash
    }

}
