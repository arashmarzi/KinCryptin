#include <pebble.h>

static Window *my_window;
static TextLayer *text_layers;

static uint32_t num_samples = 1; // number of samples per batch/callback

static void accel_data_handler(AccelData *data, uint32_t num_samples) {
  // Read sample 0's x, y, and z values
  int16_t x = data[0].x;
  int16_t y = data[0].y;
  int16_t z = data[0].z;

  // Determine if the sample occured during vibration, and when it occured
  bool did_vibrate = data[0].did_vibrate;
  uint64_t timestamp = data[0].timestamp;

  
  
  if(!did_vibrate) {
    // Print it out
    APP_LOG(APP_LOG_LEVEL_INFO, "t: %llu, x: %d, y: %d, z: %d, size: %d",
                                                          timestamp, x, y, z, (sizeof(data) / sizeof(int16_t)));
  } else {
    // Discard with a warning
    APP_LOG(APP_LOG_LEVEL_WARNING, "Vibration occured during collection");
  }
}

static void handle_init(void) {
  my_window = window_create();

  text_layers = text_layer_create(GRect(0, 0, 144, 20));
  window_stack_push(my_window, true);

  accel_service_set_sampling_rate(ACCEL_SAMPLING_50HZ);  
  accel_data_service_subscribe(num_samples, accel_data_handler);
}

static void handle_deinit(void) {
  accel_data_service_unsubscribe();
  text_layer_destroy(text_layers);
  window_destroy(my_window);
}

int main(void) {
  handle_init();
  app_event_loop();
  handle_deinit();
}
