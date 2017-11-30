library(tidyverse)

options(digits.secs = 12)

file_list <- list.files()

files_sent <- sort(file_list[grep("enviado", file_list)])
files_received <- sort(file_list[grep("recibido", file_list)])

files_df <- tibble(
  files_sent,
  files_received
)

clean_files <- function(file_name_sent, file_name_received){
  
  sent_1 <- read_csv(file_name_sent, col_names = F)
  
  num_clientes <- sent_1$X3 %>% unique() %>% length()
  num_clientes_por_paquete <- nrow(sent_1)/num_clientes
  
  sent <- sent_1 %>% 
    mutate(time_str = substr(X2, 2, 13)) %>% 
    mutate(time_str2 = stringi::stri_replace_last(time_str, ".", fixed = ":")) %>% 
    mutate(time = as.POSIXct(time_str2, format = "%H:%M:%OS")) %>% 
    select(id = X1, time_sent = time)
  
  received <- read_csv(file_name_received, col_names = F) %>% 
    mutate(time_str = substr(X2, 2, 13)) %>% 
    mutate(time_str2 = stringi::stri_replace_last(time_str, ".", fixed = ":")) %>% 
    mutate(time = as.POSIXct(time_str2, format = "%H:%M:%OS")) %>% 
    select(id = X1, time_received = time)
  
  
  
  joined <- sent %>% 
    full_join(received) %>% 
    mutate(sec_diff = as.numeric(difftime(time_received, time_sent, units = "sec"))) %>% 
    mutate(num_clientes = num_clientes,
           num_paquetes = num_clientes_por_paquete)

  return(joined)  
}

clean_list <- lapply(1:nrow(files_df), function(i){
  out <- clean_files(files_df$files_sent[i], files_df$files_received[i])
  return(out)
})
  


clean_files("paquetes_enviados_clientes_30c_50p.txt", "paquetes_recibidos_dispositivos_30c_50p.txt")

nrow(joined)/max(joined$sec_diff, na.rm = T)
sum(is.na(joined$time_received))/nrow(joined)


polling <- read.csv("Polling.csv") %>% 
  mutate(primera_hora_enviado = as.POSIXct(primera_hora_enviado, format = "%H:%M:%OS"),
         ultima_hora_enviado = as.POSIXct(ultima_hora_enviado, format = "%H:%M:%OS"),
         primera_hora_recibido = as.POSIXct(primera_hora_recibido, format = "%H:%M:%OS"),
         ultima_hora_recibido = as.POSIXct(ultima_hora_recibido, format = "%H:%M:%OS")) %>% 
  mutate(sec_diff_sent = as.numeric(difftime(ultima_hora_enviado, primera_hora_enviado, 
                                             units = "sec")),
         sec_diff_received = as.numeric(difftime(ultima_hora_recibido, primera_hora_recibido, 
                                             units = "sec"))) %>% 
  mutate(throughput_sent = Num_paquetes_total/sec_diff_sent,
         throughput_received = num_paquetes_recibidos/sec_diff_received)

polling %>% 
  ggplot() +
  geom_point(aes(throughput_sent, throughput_received)) +
  xlab("Sent packet rate (packets/sec)") +
  ylab("Received packet rate (packets/sec)") +
  theme_bw()


interrupts <- read.csv("Interrupt.csv") %>% 
  mutate(primera_hora_enviado = as.POSIXct(primera_hora_enviado, format = "%H:%M:%OS"),
         ultima_hora_enviado = as.POSIXct(ultima_hora_enviado, format = "%H:%M:%OS"),
         primera_hora_recibido = as.POSIXct(primera_hora_recibido, format = "%H:%M:%OS"),
         ultima_hora_recibido = as.POSIXct(ultima_hora_recibido, format = "%H:%M:%OS")) %>% 
  mutate(sec_diff_sent = as.numeric(difftime(ultima_hora_enviado, primera_hora_enviado, 
                                             units = "sec")),
         sec_diff_received = as.numeric(difftime(ultima_hora_recibido, primera_hora_recibido, 
                                                 units = "sec"))) %>% 
  mutate(throughput_sent = Num_paquetes_total/sec_diff_sent,
         throughput_received = num_paquetes_recibidos/sec_diff_received)

interrupts %>% 
  ggplot() +
  geom_point(aes(throughput_sent, throughput_received)) +
  xlab("Sent packet rate (packets/sec)") +
  ylab("Received packet rate (packets/sec)") +
  theme_bw()



