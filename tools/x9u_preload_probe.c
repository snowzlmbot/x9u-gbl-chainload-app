#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>

static void write_all(int fd, const char *data, size_t length) {
    while (length > 0) {
        ssize_t written = write(fd, data, length);
        if (written <= 0) {
            return;
        }
        data += written;
        length -= (size_t) written;
    }
}

__attribute__((constructor)) static void x9u_preload_probe(void) {
    const char *path = getenv("X9U_PROBE_LOG");
    if (path == NULL || path[0] == '\0') {
        return;
    }

    int fd = open(path, O_WRONLY | O_CREAT | O_APPEND | O_CLOEXEC, 0600);
    if (fd < 0) {
        return;
    }

    static const char marker[] = "X9U_PRELOAD_PROBE_OK\n";
    write_all(fd, marker, sizeof(marker) - 1);
    fsync(fd);
    close(fd);
}