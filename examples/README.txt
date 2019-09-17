This is a list of the models in this directory,
together with the number of reachable states in the synchronoys product
(if known) and the expected result of a conflict check.

Directory and model          #States Nconf. Notes
=============================================================================
simple/
  empty_1                          1   yes  1-state automaton, nonblocking
  empty_2                          1   no   1-state automaton, blocking
  small_factory_2                 12   yes  Small factory
  bad_factory                     15   no
  bfactory                        69   yes  Big factory (3 machines, 1 buffer)
  cell                            56   yes  Little manufacturing cell
  cell_block                      64   no
  debounce                         6   yes
  elevator_safety                906   yes  Elevator models
  elevator_liveness              356   yes
  ipc                          20592   no  Intertwined product cycles
  ipc_cswitch                  41184   no    manufacturing system
  ipc_lswitch                 247104   no
  ipc_lswitch_sup               4374   yes
  ipc_uswitch                 370656   no
  tictactoe                     6324   yes  Tic-tac-toe game without supervisor
  wsp_timer                        5   no   2 automata from BMW central locking
  wsp_timer_noreset                4   yes
agv/                                        Automated Guided Vehicles system
  agv                       25731072   yes
  agvb                      22929408   no
batch_tank/                                 Batch tank (assignment 1)
  batch_plant                     48   yes  Batch tank plant with jelly spec
  amk14                           40   yes  Various attempts at solution
  cjn5                            52   yes
  jbr2                            38   no
  kah18                           62   yes
  lsr1_1                          55   no
  rch11                          512   yes
  scs10                          138   yes
  smr26                           42   yes
  tk27                            18   yes
  tp20                           111   yes
bmw/                                        BMW automotive controllers
  big_bmw                   31393656   yes  BMW window lift controller
  bmw_fh                        7672   yes
  dreitueren                  420283   yes  Parts of central locking system
  ftuer                          195   yes
  koordwsp                    465648   yes
  koordwsp_block              634608   no
  tuer1                          226   yes
  tuer2                          238   yes
  vtueren                       8407   yes
profisafe/                                  ProfiSAFE communication protocol
  profisafe_i4host_efa        508780   yes    (note: these models may take
  profisafe_i4host_efa_block  508772   no     several seconds to load)
  profisafe_i5host_efa        886200   yes
  profisafe_i5host_efa_block  886190   no
  profisafe_i6host_efa       1416428   yes
  profisafe_i6host_efa_block 1416416   no
  profisafe_i4slave_efa        17226   yes
philo/                                      Dining philosophers
  nbl_philosophers_05          11911   yes  Dijkstra's classical problem
  dining_philosophers_05        1973   no   Blocking version
  dining_philosophers_06        9007   no
  dining_philosophers_07       41093   no
  dining_philosophers_08      187455   no
  dining_philosophers_09      855093   no
  dining_philosophers_10     3900559   no
  dining_philosophers_11           ?   no
  dining_philosophers_12           ?   no
  dirty_philosophers_05         3123   no   Variation
  dirty_philosophers_06        15623   no
  dirty_philosophers_07        78123   no
  dirty_philosophers_08       390623   no
  dirty_philosophers_09      1953123   no
  dirty_philosophers_10      9765623   no
  dirty_philosophers_11            ?   no
  dirty_philosophers_12            ?   no
transfer_line/                              Transfer line
  tline_1                         28   yes      parametrisable manufacturing
  tline_1_block                   45   no       system
  tline_2                        410   yes
  tline_2_block                 1045   no
  tline_2a_block                  97   no
  tline_3                       5992   yes
  tline_3_block                15269   no
  tline_4                      87578   yes
  tline_4_block               223171   no
  tline_5                    1280020   yes
  tline_5_block              3261815   no
  tline_6                   18708482   yes
  tline_6_block             47673949   no
