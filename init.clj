#!/usr/bin/env boot

(set-env! :dependencies '[[pieterbreed/tappit "0.9.8"]
                          [me.raynes/conch "0.8.0"]
                          [environ "1.0.3"]])


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

  (diag! "houstan-local-vagrant/init...")
  (conch/let-programs [init (-> work-dir
                                (clojure.java.io/file "houstan-local-vagrant" "init.clj")
                                .getCanonicalPath)]
    (let [init-proc (init {:seq true
                           :throw false
                           :verbose true})]
      (diag-lines (-> init-proc :proc :out))
      (if (not (=! 0 (-> init-proc :exit-code deref)
                   "local-vagrant"))
        (do 
          (bail-out! "local vagrant failed")
          (System/exit 1)))))

  ;; ----------------------------------------

  (diag! "houstan-local-datomic/init...")
  (conch/let-programs [init (-> work-dir
                                (clojure.java.io/file "houstan-local-datomic" "init.clj")
                                .getCanonicalPath)]
    (let [init-proc (init {:seq true
                           :throw false
                           :verbose true})]
      (diag-lines (-> init-proc :proc :out))
      (if (not (=! 0 (-> init-proc :exit-code deref)
                   "local-datomic"))
        (do 
          (bail-out! "local datomic failed")
          (System/exit 1)))))





  )
