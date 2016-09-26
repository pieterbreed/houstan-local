#!/usr/bin/env boot

(set-env! :dependencies '[[pieterbreed/tappit "0.9.8"]
                          [me.raynes/conch "0.8.0"]
                          [environ "1.0.3"]
                          [pieterbreed/yoostan-lib "0.0.1-SNAPSHOT"]])


;; ----------------------------------------

;; warnings, drama. we need an unreleased version of clojure for this script
(require 'environ.core)
(when (not (re-find #"1\.9\.0"
                    (environ.core/env :boot-clojure-version)))
  (println "# Set ENV variables like this:")
  (println "# $ export BOOT_CLOJURE_VERSION=1.9.0-alpha10")
  (println "Bail out! Requires BOOT_CLOJURE_VERSION=1.9.0-alpha10 or higher")
  (System/exit 0))

;; ----------------------------------------

(require '[tappit.producer :refer [with-tap! ok]])
(require '[me.raynes.conch :as conch])
(require '[me.raynes.conch.low-level :as sh])
(require '[yoostan-lib.utils :as utils])

;; ----------------------------------------

(let [bsf (-> boot.core/*boot-script* clojure.java.io/file)]
  (def work-dir
    (-> (or (if (.isAbsolute bsf) bsf)
            (clojure.java.io/file (System/getProperty "user.dir")
                                  bsf))
        .getParentFile
        .getCanonicalPath)))

;; ----------------------------------------

(with-tap!

  (defn diag-lines [ll] (->> ll (map diag!) dorun))

  ;; ----------------------------------------

  (diag! (str "working directory at: " work-dir))
  (diag-lines ["PLEASE BE PATIENT WHILE THE SCRIPTS RUN..."])

  ;; ----------------------------------------

  (diag! "houstan-local-vagrant/start...")
  (conch/let-programs [start (-> work-dir
                                 (clojure.java.io/file "houstan-local-vagrant" "start.clj")
                                 .getCanonicalPath)]
    (let [start-proc (start {:seq true
                             :throw false
                             :verbose true})]
      (diag-lines (-> start-proc :proc :out))
      (if (not (=! 0 (-> start-proc :exit-code deref)
                   "local-vagrant"))
        (do 
          (bail-out! "local vagrant failed")
          (System/exit 1)))))

  ;; ----------------------------------------

  (diag! "houstan-local-datomic/start...")
  (conch/let-programs [start (-> work-dir
                                 (clojure.java.io/file "houstan-local-datomic" "start.clj")
                                 .getCanonicalPath)]
    (let [start-proc (start {:seq true
                             :throw false
                             :verbose true})]
      (diag-lines (-> start-proc :proc :out))
      (if (not (=! 0 (-> start-proc :exit-code deref)
                   "local-datomic"))
        (do 
          (bail-out! "local datomic failed")
          (System/exit 1)))))





  )
